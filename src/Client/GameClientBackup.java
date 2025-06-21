package Client;

import Constants.GameState;
import Constants.PlayerColour;
import gameObjects.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class GameClientBackup {

    private ClientSideConnection csc;
    private Player player;
    private HashMap<ArrayList<Integer>, Tile> tilesDict;
    private HashMap<ArrayList<Integer>, Node> nodesDict;
    private HashMap<ArrayList<Integer>, Town> townsDict;
    private HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;
    private GameState gameState;
    private int currentPlayerTurn;

    public void connectToServer() {
        csc = new ClientSideConnection();
        int playerID = csc.playerID;
        PlayerColour playerColour = switch (playerID) {
            case 1 -> PlayerColour.RED;
            case 2 -> PlayerColour.BLUE;
            case 3 -> PlayerColour.GREEN;
            default -> PlayerColour.ORANGE;
        };
        this.player = new Player(playerColour, playerID);
        this.gameState = GameState.LOBBY;

        csc.sendPlayer(this.player);
        //csc.waitForStartSignal();
        csc.listenForServerUpdates();
    }

    public void addSettlement(HashMap<ArrayList<Integer>, Node> newNodes, HashMap<ArrayList<Integer>, Town> newTowns, Player newPlayer) {
        this.nodesDict = newNodes;
        this.townsDict = newTowns;
        this.player = newPlayer;

        csc.addSettlement(this.player);
    }

    public void upgradeToCity() {

    }

    public void addRoad() {

    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public HashMap<ArrayList<Integer>, Tile> getTilesDict() {
        return tilesDict;
    }

    public void setTilesDict(HashMap<ArrayList<Integer>, Tile> tilesDict) {
        this.tilesDict = tilesDict;
    }

    public HashMap<ArrayList<Integer>, Node> getNodesDict() {
        return nodesDict;
    }

    public void setNodesDict(HashMap<ArrayList<Integer>, Node> nodesDict) {
        this.nodesDict = nodesDict;
    }

    public HashMap<ArrayList<Integer>, Town> getTownsDict() {
        return townsDict;
    }

    public void setTownsDict(HashMap<ArrayList<Integer>, Town> townsDict) {
        this.townsDict = townsDict;
    }

    public HashMap<ArrayList<ArrayList<Integer>>, Road> getRoadsDict() {
        return roadsDict;
    }

    public void setRoadsDict(HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict) {
        this.roadsDict = roadsDict;
    }

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public class ClientSideConnection {
        private Socket socket;
        private ObjectInputStream dataIn;
        private ObjectOutputStream dataOut;
        private int playerID;

        public ClientSideConnection() {
            System.out.println("---Client---");
            try {
                socket = new Socket("25.47.99.90", 44444);
                dataOut = new ObjectOutputStream(socket.getOutputStream());
                dataOut.flush();
                dataIn = new ObjectInputStream(socket.getInputStream());
                playerID = dataIn.readInt();
                tilesDict = (HashMap<ArrayList<Integer>, Tile>) dataIn.readObject();
                nodesDict = (HashMap<ArrayList<Integer>, Node>) dataIn.readObject();
                townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                roadsDict = (HashMap<ArrayList<ArrayList<Integer>>, Road>) dataIn.readObject();
                currentPlayerTurn = dataIn.readInt();

            } catch (IOException e) {
                System.out.println("IO Exception occurred from CSC Constructor: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        public void sendPlayer(Player player) {
            try {
                dataOut.writeObject("NEW_PLAYER");
                dataOut.writeObject(player);
                dataOut.flush();
            } catch (IOException e) {
                System.out.println("IOException from sendPlayer() CSC");
            }
        }
        public void waitForStartSignal() {
            try {
                Object obj = dataIn.readObject();
                System.out.println(obj);
                if (obj instanceof GameState state && state == GameState.INITIAL_PLACEMENT) {
                    gameState = GameState.INITIAL_PLACEMENT;
                }
            } catch (IOException e) {
                System.out.println("IOException occurred from waitForStartSignal() CSC");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        public void addSettlement(Player player) {
            try {
                dataOut.writeObject("NEW_TOWN");
                dataOut.writeObject(nodesDict);
                dataOut.writeObject(townsDict);
                dataOut.writeObject(player);
                dataOut.flush();
            } catch (IOException e) {
                System.out.println("TOException occurred from addSettlement() CSC");
            }

        }

        public void upgradeToCity() {

        }

        public void addRoad() {

        }

        public void listenForServerUpdates() {
            new Thread(() -> {
                try {
                    while (true) {
                        Object msg = dataIn.readObject();
                        if (msg instanceof String command) {
                            switch (command) {
                                case "INITIAL_PLACEMENT":
                                    gameState = (GameState) dataIn.readObject();
                                    System.out.println("Yay");
                                case "NEW_TOWN_ADDED":
                                    nodesDict = (HashMap<ArrayList<Integer>, Node>) dataIn.readObject();
                                    townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                                    break;
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception in listenForServerUpdates() CSC");
                }
            }).start();
        }
    }
}
