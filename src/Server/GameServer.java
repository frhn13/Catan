package Server;

import Constants.CSMessages;
import Constants.GameState;
import Constants.ResourceType;
import gameObjects.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static Constants.SCMessages.*;

public class GameServer {

    private ServerSocket serverSocket;
    GameState gameState = GameState.LOBBY;
    private int numPlayers;
    private int diceValue;
    private ArrayList<Player> allPlayers = new ArrayList<>();
    private HashSet<Integer> resetList = new HashSet<>();
    private ServerSideConnection player1;
    private ServerSideConnection player2;
    private ServerSideConnection player3;
    private ServerSideConnection player4;

    private HashMap<ResourceType, Integer> currentPlayerTakingTrade;
    private HashMap<ResourceType, Integer> currentPlayerGivingTrade;
    private Player currentPlayer;

    public GameServer() {
        System.out.println("--Game Server--");
        numPlayers = 0;
        try {
            serverSocket = new ServerSocket(44444);
        } catch (IOException ex) {
            System.out.println("IO Exception occurred");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");
            while (numPlayers < 4) {
                Socket socket = serverSocket.accept();
                numPlayers++;
                ServerSideConnection ssc = new ServerSideConnection(socket, numPlayers);
                System.out.println("Player Number " + numPlayers + " has connected.");
                switch (numPlayers) {
                    case 1 -> player1 = ssc;
                    case 2 -> player2 = ssc;
                    case 3 -> player3 = ssc;
                    default -> player4 = ssc;
                }
                Thread t = new Thread(ssc);
                t.start();
            }

            // Wait for all threads to be ready
            while (!(player1.isReady() && player2.isReady() && player3.isReady() && player4.isReady())) {
                Thread.sleep(50); // Wait briefly, avoid tight loop
            }

            gameState = GameState.INITIAL_PLACEMENT;
            player1.startGame();
            player2.startGame();
            player3.startGame();
            player4.startGame();
            System.out.println("Now have 4 players. No longer accepting players");

        } catch (IOException e) {
            System.out.println("IO Exception from acceptConnections()");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class ServerSideConnection implements Runnable {
        private Socket socket;
        private ObjectInputStream dataIn;
        private ObjectOutputStream dataOut;
        private int playerID;
        private boolean ready = false;

        public boolean isReady() {
            return ready;
        }
        public void setReady(boolean ready) {
            this.ready = ready;
        }

        public ServerSideConnection(Socket s, int id) {
            socket = s;
            playerID = id;
            try {
                dataOut = new ObjectOutputStream(socket.getOutputStream());
                dataOut.flush();
                dataIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("IOException from run() SSC");
            }
        }
        public void run() {
            try {
                dataOut.writeInt(playerID);
                dataOut.writeObject(GameBoard.getTilesDict());
                dataOut.writeObject(GameBoard.getNodesDict());
                dataOut.writeObject(GameBoard.getTownsDict());
                dataOut.writeObject(GameBoard.getRoadsDict());
                dataOut.writeInt(GameBoard.getCurrentPlayerTurn());
                // dataOut.writeObject(gameState);
                dataOut.flush();

                while (true) {
                    // receive move, apply to GameBoard, broadcast if needed
                    Object msg = dataIn.readObject();

                    if (msg instanceof CSMessages command) {
                        Player newPlayer;
                        switch (command) {
                            case NEW_PLAYER:
                                GameBoard.updatePlayers((Player) dataIn.readObject());
                                // allPlayers.add((Player) dataIn.readObject());
                                ready = true;
                                break;
                            case START_NORMAL_GAME:
                                gameState = GameState.NORMAL_PLAY;
                                broadcastNormalGame();
                                break;
                            case NEW_RESOURCES:
                                newPlayer = (Player) dataIn.readObject();
                                GameBoard.updatePlayers(newPlayer);
                                broadcastNewResources();
                                break;
                            case GET_RESOURCES:
                                diceValue = dataIn.readInt();
                                broadcastGettingResources();
                                break;
                            case NEW_TOWN:
                                GameBoard.setNodesDict((HashMap<ArrayList<Integer>, Node>) dataIn.readObject());
                                GameBoard.setTownsDict((HashMap<ArrayList<Integer>, Town>) dataIn.readObject());

                                newPlayer = (Player) dataIn.readObject();
                                GameBoard.updatePlayers(newPlayer);
                                broadcastNewTown();
                                break;
                            case NEW_CITY:
                                GameBoard.setTownsDict((HashMap<ArrayList<Integer>, Town>) dataIn.readObject());
                                newPlayer = (Player) dataIn.readObject();
                                GameBoard.updatePlayers(newPlayer);
                                broadcastNewCity();
                                break;
                            case NEW_ROAD:
                                GameBoard.setRoadsDict((HashMap<ArrayList<ArrayList<Integer>>, Road>) dataIn.readObject());
                                newPlayer = (Player) dataIn.readObject();
                                GameBoard.updatePlayers(newPlayer);
                                broadcastNewRoad();
                                break;
                            case NEW_TRADE:
                                currentPlayerTakingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                currentPlayerGivingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                currentPlayer = (Player) dataIn.readObject();
                                System.out.println(currentPlayerTakingTrade);
                                System.out.println(currentPlayerGivingTrade);
                                broadcastNewTradeOffer();
                                break;
                            case NEW_TRADE_ACCEPTED:
                                currentPlayerGivingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                currentPlayerTakingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                currentPlayer = (Player) dataIn.readObject();
                                broadcastTradeAcceptance();
                                break;
                            case NEW_TURN:
                                GameBoard.setCurrentPlayerTurn(dataIn.readInt());
                                System.out.println("Current Turn: " + GameBoard.getCurrentPlayerTurn());
                                broadcastNewTurn();
                                break;
                            case END_GAME:
                                gameState = GameState.ENDGAME;
                                broadcastEndGame();
                                break;
                            case NEW_GAME:
                                newPlayer = (Player) dataIn.readObject();
                                resetList.add(newPlayer.getPlayerNumber());
                                if (resetList.size() >= 4) {
                                    gameState = GameState.LOBBY;
                                    GameBoard.resetGameBoard();
                                    resetPlayers();
                                }
                                break;
                            case ROBBER_MOVED:
                                GameBoard.setTilesDict((HashMap<ArrayList<Integer>, Tile>) dataIn.readObject());
                                broadcastRobberMoved();
                                break;
                            case PLAYER_ROBBED:
                                boolean canBeRobbed = false;
                                Random random = new Random();
                                Player robbingPlayer = (Player) dataIn.readObject();
                                Player robbedPlayer = (Player) dataIn.readObject();
                                ResourceType[] resources = ResourceType.values();

                                for (Integer resourceAmount : robbedPlayer.getPlayerResourcesDict().values()) {
                                    if (resourceAmount > 0) {
                                        canBeRobbed = true;
                                        break;
                                    }
                                }

                                System.out.println("Can be robbed");
                                System.out.println(canBeRobbed);

                                if (canBeRobbed) {
                                    while (true) {
                                        ResourceType randomResource = resources[random.nextInt(resources.length)];
                                        HashMap<ResourceType, Integer> resourceChange = new HashMap<>();

                                        if (robbedPlayer.getPlayerResourcesDict().get(randomResource) > 0) {
                                            System.out.println(randomResource);
                                            System.out.println(robbedPlayer.getPlayerResourcesDict().get(randomResource));
                                            resourceChange.put(randomResource, -1);
                                            robbedPlayer.updatePlayerResourcesDict(resourceChange);
                                            resourceChange.put(randomResource, 1);
                                            robbingPlayer.updatePlayerResourcesDict(resourceChange);
                                            break;
                                        }
                                    }

                                    GameBoard.updatePlayers(robbingPlayer);
                                    GameBoard.updatePlayers(robbedPlayer);
                                }
                                broadcastPlayerRobbed();
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException from run() SSC " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }



        public void startGame() {
            try {
                dataOut.writeObject(INITIAL_PLACEMENT);
                dataOut.writeObject(gameState);
                dataOut.flush();
            } catch (IOException e) {
                System.out.println("IOException from startGame() SSC");
            }
        }

        private void broadcastNormalGame() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NORMAL_GAME_STARTED);
                    ssc.dataOut.writeObject(gameState);
                    //ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastNormalGame() SSC");
            }
        }

        public void broadcastNewResources() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    synchronized (ssc) {
                        ssc.dataOut.writeObject(NEW_RESOURCES_ADDED);
                        ssc.dataOut.reset();
                        ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                        ssc.dataOut.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastNewResources() CSC" + e.getMessage());
            }
        }

        public void broadcastGettingResources() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(GET_RESOURCES_ADDED);
                    ssc.dataOut.writeInt(diceValue);
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastGettingResources() CSC");
            }
        }

        public void broadcastNewTown() {
            try {
                for (Player player : GameBoard.getAllPlayers()) {
                    System.out.println("Number: " + player.getPlayerNumber() + " Colour: " + player.getPlayerColour() + " Score: " + player.getScore() + " Towns: " + player.getPlayerTownsDict());
                }
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NEW_TOWN_ADDED);
                    ssc.dataOut.writeObject(GameBoard.getNodesDict());
                    ssc.dataOut.writeObject(GameBoard.getTownsDict());
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                    System.out.println("broadcastNewTown SSC");
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastNewTown() SSC");
            }
        }

        public void broadcastNewCity() {
            try {
                for (Player player : GameBoard.getAllPlayers()) {
                    System.out.println("Number: " + player.getPlayerNumber() + " Colour: " + player.getPlayerColour() + " Score: " + player.getScore() + " Towns: " + player.getPlayerTownsDict());
                }
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NEW_CITY_ADDED);
                    ssc.dataOut.writeObject(GameBoard.getTownsDict());
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                    System.out.println("broadcastNewCity SSC");
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastNewCity() SSC");
            }
        }

        public void broadcastNewRoad() {
            try {
                for (Player player : GameBoard.getAllPlayers()) {
                    System.out.println("Number: " + player.getPlayerNumber() + " Colour: " + player.getPlayerColour() + " Score: " + player.getScore() + " Towns: " + player.getPlayerTownsDict());
                }
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NEW_ROAD_ADDED);
                    ssc.dataOut.writeObject(GameBoard.getRoadsDict());
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastNewRoad() SSC");
            }
            System.out.println("broadcastNewRoad SSC");
        }

        public void broadcastNewTradeOffer() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NEW_TRADE_ADDED);
                    ssc.dataOut.writeObject(currentPlayerTakingTrade);
                    ssc.dataOut.writeObject(currentPlayerGivingTrade);
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(currentPlayer);
                    ssc.dataOut.flush();
                }
            } catch (Exception e) {
                System.out.println("IOException from broadNewTradeOffer() SSC");
            }
        }

        public void broadcastTradeAcceptance() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(ACCEPTED_TRADE_ADDED);
                    ssc.dataOut.writeObject(currentPlayerTakingTrade);
                    ssc.dataOut.writeObject(currentPlayerGivingTrade);
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(currentPlayer);
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastTradeAcceptance() SSC");
            }
        }

        public void broadcastNewTurn() {
            try {
                for (Player player : GameBoard.getAllPlayers()) {
                    System.out.println("Number: " + player.getPlayerNumber() + " Colour: " + player.getPlayerColour() + " Score: " + player.getScore() + " Towns: " + player.getPlayerTownsDict());
                }
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(NEW_TURN_ADDED);
                    ssc.dataOut.writeInt(GameBoard.getCurrentPlayerTurn());
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                }
                System.out.println("broadcastNewTurn SSC");
            } catch (IOException e) {
                System.out.println("IOException from broadcastNewTurn() SSC");
            }
        }

        public void broadcastEndGame() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(END_GAME_ADDED);
                    ssc.dataOut.writeObject(gameState);
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                    ssc.setReady(false);
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastEndGame() SSC");
            }
        }

        public void resetPlayers() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(RESET_PLAYERS);
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameState.INITIAL_PLACEMENT);
                    ssc.dataOut.writeObject(GameBoard.getTilesDict());
                    ssc.dataOut.writeObject(GameBoard.getNodesDict());
                    ssc.dataOut.writeObject(GameBoard.getTownsDict());
                    ssc.dataOut.writeObject(GameBoard.getRoadsDict());
                    ssc.dataOut.writeInt(GameBoard.getCurrentPlayerTurn());
                    ssc.dataOut.flush();
                    ssc.setReady(true);
                    resetList = new HashSet<>();
                }
            } catch (IOException e) {
                System.out.println("IOException from resetPlayers() SSC");
            }
        }

        public void broadcastRobberMoved() {
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(ROBBER_MOVE_ADDED);
                    ssc.dataOut.writeObject(GameBoard.getTilesDict());
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastRobberMoved() SSC");
            }
        }

        public void broadcastPlayerRobbed() {
            System.out.println("Yay");
            try {
                for (ServerSideConnection ssc : List.of(player1, player2, player3, player4)) {
                    ssc.dataOut.writeObject(PLAYER_ROB_ADDED);
                    ssc.dataOut.reset();
                    ssc.dataOut.writeObject(GameBoard.getAllPlayers());
                    ssc.dataOut.flush();
                }
            } catch (IOException e) {
                System.out.println("IOException from broadcastPlayerRobbed() SSC");
            }
        }
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }
}
