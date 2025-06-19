package Client;

import Constants.PlayerColour;
import gameObjects.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class GameClient {

    private ClientSideConnection csc;
    private int playerID;
    private PlayerColour playerColour;
    private HashMap<ArrayList<Integer>, Tile> tilesDict;
    private HashMap<ArrayList<Integer>, Node> nodesDict;
    private HashMap<ArrayList<Integer>, Town> townsDict;
    private HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;

    public void connectToServer() {
        csc = new ClientSideConnection();
        System.out.println("Yo");
        this.playerID = csc.playerID;
        switch (this.playerID) {
            case 1:
                this.playerColour = PlayerColour.RED;
                break;
            case 2:
                this.playerColour = PlayerColour.BLUE;
                break;
            case 3:
                this.playerColour = PlayerColour.GREEN;
                break;
            default:
                this.playerColour = PlayerColour.ORANGE;
                break;
        }
        this.tilesDict = csc.tilesDict;
        this.nodesDict = csc.nodesDict;
        this.townsDict = csc.townsDict;
        this.roadsDict = csc.roadsDict;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public PlayerColour getPlayerColour() {
        return playerColour;
    }

    public void setPlayerColour(PlayerColour playerColour) {
        this.playerColour = playerColour;
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

    public class ClientSideConnection {
        private Socket socket;
        private DataInputStream intDataIn;
        private DataOutputStream intDataOut;
        private ObjectInputStream dataIn;
        private ObjectOutputStream dataOut;
        private int playerID;
        private HashMap<ArrayList<Integer>, Tile> tilesDict;
        private HashMap<ArrayList<Integer>, Node> nodesDict;
        private HashMap<ArrayList<Integer>, Town> townsDict;
        private HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;

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

            } catch (IOException e) {
                System.out.println("IO Exception occurred from CSC Constructor");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
