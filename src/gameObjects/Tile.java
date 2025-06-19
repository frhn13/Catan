package gameObjects;

import Constants.ResourceType;

import java.io.Serializable;
import java.util.ArrayList;

public class Tile implements Serializable {

    private ArrayList<Integer> tileCoordinates = new ArrayList<>();
    private ResourceType tileResource;
    private int rollValue;
    private ArrayList<ArrayList<Integer>> correspondingNodeCoordinates;

    public Tile(ArrayList<Integer> tileCoordinates, ResourceType tileResource, int rollValue, ArrayList<ArrayList<Integer>> correspondingNodeCoordinates) {
        this.tileCoordinates = tileCoordinates;
        this.tileResource = tileResource;
        this.rollValue = rollValue;
        this.correspondingNodeCoordinates = correspondingNodeCoordinates;
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

    public ArrayList<ArrayList<Integer>> getCorrespondingNodeCoordinates() {
        return correspondingNodeCoordinates;
    }

    public void setCorrespondingNodeCoordinates(ArrayList<ArrayList<Integer>> correspondingNodeCoordinates) {
        this.correspondingNodeCoordinates = correspondingNodeCoordinates;
    }
}
