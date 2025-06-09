package gameObjects;

import java.util.ArrayList;

public class Node {
    private ArrayList<Integer> nodeCoordinates;
    private ArrayList<Node> connectedNodes;
    private ArrayList<Tile> connectedTiles;
    private boolean hasSettlement;

    public Node(ArrayList<Integer> nodeCoordinates, ArrayList<Node> connectedNodes, ArrayList<Tile> connectedTiles) {
        this.nodeCoordinates = nodeCoordinates;
        this.connectedNodes = connectedNodes;
        this.connectedTiles = connectedTiles;
        this.hasSettlement = false;
    }

    public ArrayList<Integer> getNodeCoordinates() {
        return nodeCoordinates;
    }

    public void setNodeCoordinates(ArrayList<Integer> nodeCoordinates) {
        this.nodeCoordinates = nodeCoordinates;
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

    public boolean isHasSettlement() {
        return hasSettlement;
    }

    public void setHasSettlement(boolean hasSettlement) {
        this.hasSettlement = hasSettlement;
    }
}
