package gameObjects;

import Constants.PlayerColour;

import java.io.Serializable;
import java.util.ArrayList;

public class Town implements Serializable {
    private ArrayList<Integer> townCoordinates;
    private ArrayList<Node> connectedNodes;
    private ArrayList<Tile> connectedTiles;
    private PlayerColour townColour;
    private ArrayList<Integer> townBoardCoordinates;
    private boolean isCity;

    public Town(ArrayList<Integer> townCoordinates, ArrayList<Node> connectedNodes, ArrayList<Tile> connectedTiles, PlayerColour townColour, ArrayList<Integer> townBoardCoordinates) {
        this.townCoordinates = townCoordinates;
        this.connectedNodes = connectedNodes;
        this.connectedTiles = connectedTiles;
        this.townColour = townColour;
        this.townBoardCoordinates = townBoardCoordinates;
        this.isCity = false;
    }

    public ArrayList<Integer> getTownCoordinates() {
        return townCoordinates;
    }

    public void setTownCoordinates(ArrayList<Integer> townCoordinates) {
        this.townCoordinates = townCoordinates;
    }

    public ArrayList<Node> getConnectedNodes() {
        return connectedNodes;
    }

    public void setConnectedNodes(ArrayList<Node> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public ArrayList<Tile> getConnectedTiles() {
        return connectedTiles;
    }

    public void setConnectedTiles(ArrayList<Tile> connectedTiles) {
        this.connectedTiles = connectedTiles;
    }

    public PlayerColour getTownColour() {
        return townColour;
    }

    public void setTownColour(PlayerColour townColour) {
        this.townColour = townColour;
    }

    public ArrayList<Integer> getTownBoardCoordinates() {
        return townBoardCoordinates;
    }

    public void setTownBoardCoordinates(ArrayList<Integer> townBoardCoordinates) {
        this.townBoardCoordinates = townBoardCoordinates;
    }

    public boolean isCity() {
        return isCity;
    }

    public void setCity(boolean city) {
        isCity = city;
    }
}
