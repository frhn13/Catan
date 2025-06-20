package Server;

import Constants.GameState;
import gameObjects.GameBoard;
import gameObjects.Player;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class GameServer {

    private ServerSocket serverSocket;
    GameState gameState = GameState.LOBBY;
    private int numPlayers;
    private ArrayList<Player> allPlayers = new ArrayList<>();
    private ServerSideConnection player1;
    private ServerSideConnection player2;
    private ServerSideConnection player3;
    private ServerSideConnection player4;

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

            System.out.println("Now have 4 players. No longer accepting players");
            gameState = GameState.INITIAL_PLACEMENT;
            player1.startGame();
            player2.startGame();
            player3.startGame();
            player4.startGame();

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

                Object obj = dataIn.readObject();
                if (obj instanceof Player player) {
                    allPlayers.add(player);
                    System.out.println("Received player: " + player.getPlayerNumber());
                }
                ready = true;
                while (true) {
                    // receive move, apply to GameBoard, broadcast if needed
                }
            } catch (IOException e) {
                System.out.println("IOException from run() SSC" + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void startGame() {
            try {
                dataOut.writeObject(GameState.INITIAL_PLACEMENT);
                dataOut.flush();
            } catch (IOException e) {
                System.out.println("IOException from startGame() SSC");
            }
        }
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }
}
