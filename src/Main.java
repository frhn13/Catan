import gameObjects.GameBoard;
import gameObjects.Node;
import gameObjects.Tile;
import gameVisualisation.MainGame;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Main {
    private ClientSideConnection csc;
    private int playerID;
    private int otherPlayer;

    public void connectToServer() {
        csc = new ClientSideConnection();
    }

    private class ClientSideConnection {
        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientSideConnection() {
            System.out.println("---Client---");
            try {
                socket = new Socket("localhost", 44444);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                playerID = dataIn.readInt();
                System.out.println("Connected to server as Player #"+playerID+".");
            } catch (IOException e) {
                System.out.println("IO Exception occurred from CSC Constructor");
            }
        }
    }
    public static void main(String[] args) {
        Main m = new Main();
        m.connectToServer();
        new MainGame();
    }

}
