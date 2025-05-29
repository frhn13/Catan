import gameObjects.GameBoard;
import gameObjects.Tile;

public static void main(String[] args) {
    for (Tile tile : GameBoard.tiles) {
        System.out.println("[" + tile.getTileCoordinates().getFirst() + "," + tile.getTileCoordinates().getLast() + "], " + tile.getTileResource() + ", " + tile.getRollValue());
    }
}