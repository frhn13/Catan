package gameObjects;

import Constants.PlayerColour;

import java.util.ArrayList;

public class Road {
    private ArrayList<ArrayList<Integer>> roadNodeCoordinates;
    private ArrayList<Node> roadNodes;
    private PlayerColour roadColour;

    public Road(ArrayList<ArrayList<Integer>> roadNodeCoordinates, ArrayList<Node> roadNodes, PlayerColour roadColour) {
        this.roadNodeCoordinates = roadNodeCoordinates;
        this.roadNodes = roadNodes;
        this.roadColour = roadColour;
    }
}
