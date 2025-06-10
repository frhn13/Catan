import gameObjects.GameBoard;
import gameObjects.Tile;

import java.util.ArrayList;

public static void main(String[] args) {
    for (ArrayList<Integer> tile : GameBoard.getTilesDict().keySet()) {
        System.out.println("[" + GameBoard.getTilesDict().get(tile).getTileCoordinates().getFirst() + ", " + GameBoard.getTilesDict().get(tile).getTileCoordinates().getLast() + "], " + GameBoard.getTilesDict().get(tile).getTileResource() + ", " + GameBoard.getTilesDict().get(tile).getRollValue());
        for (ArrayList<Integer> node: GameBoard.getTilesDict().get(tile).getCorrespondingNodeCoordinates()) {
            System.out.println(node);
        }
    }
    System.out.println();

    for (ArrayList<Integer> node : GameBoard.getNodesDict().keySet()) {
        System.out.println("Node:");
        System.out.println("[" + GameBoard.getNodesDict().get(node).getNodeCoordinates().getFirst() + ", " + GameBoard.getNodesDict().get(node).getNodeCoordinates().getLast() + "]");
        System.out.println("Tiles:");
        for (Tile tile: GameBoard.getNodesDict().get(node).getConnectedTiles()) {
            System.out.println(tile.getTileCoordinates());
        }
        System.out.println();
    }
}