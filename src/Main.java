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
}