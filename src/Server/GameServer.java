package Server;

import gameObjects.GameBoard;

import java.io.*;
import java.net.*;

public class GameServer {

    private ServerSocket serverSocket;
    private int numPlayers;
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
            System.out.println("Now have 4 players. No longer accepting players");
        } catch (IOException e) {
            System.out.println("IO Exception from acceptConnections()");
        }
    }

    private class ServerSideConnection implements Runnable {
        private Socket socket;
        private ObjectInputStream dataIn;
        private ObjectOutputStream dataOut;
        private int playerID;

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
                dataOut.writeObject(GameBoard.getCurrentPlayerTurn());
                dataOut.flush();

                while (true) {
                    // receive move, apply to GameBoard, broadcast if needed
                }
            } catch (IOException e) {
                System.out.println("IOException from run() SSC" + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }
}
