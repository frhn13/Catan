package gameObjects;

import Constants.PlayerColour;

import java.util.ArrayList;

public class Town {
    private ArrayList<Integer> townCoordinates;
    private ArrayList<Node> connectedNodes;
    private ArrayList<Tile> connectedTiles;
    private PlayerColour townColour;
    private boolean isCity;

    public Town(ArrayList<Integer> townCoordinates, ArrayList<Node> connectedNodes, ArrayList<Tile> connectedTiles, PlayerColour townColour) {
        this.townCoordinates = townCoordinates;
        this.connectedNodes = connectedNodes;
        this.connectedTiles = connectedTiles;
        this.townColour = townColour;
        this.isCity = false;
    }
}
