package gameObjects;

import java.util.ArrayList;

public class Tile {

    private ArrayList<Integer> tileCoordinates = new ArrayList<>();
    private ResourceType tileResource;
    private int rollValue;

    public Tile(ArrayList<Integer> tileCoordinates, ResourceType tileResource, int rollValue) {
        this.tileCoordinates = tileCoordinates;
        this.tileResource = tileResource;
        this.rollValue = rollValue;
    }

    public ArrayList<Integer> getTileCoordinates() {
        return tileCoordinates;
    }

    public void setTileCoordinates(ArrayList<Integer> tileCoordinates) {
        this.tileCoordinates = tileCoordinates;
    }

    public ResourceType getTileResource() {
        return tileResource;
    }

    public void setTileResource(ResourceType tileResource) {
        this.tileResource = tileResource;
    }

    public int getRollValue() {
        return rollValue;
    }

    public void setRollValue(int rollValue) {
        this.rollValue = rollValue;
    }
}
