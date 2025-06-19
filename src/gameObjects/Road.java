package gameObjects;

import Constants.PlayerColour;

import java.io.Serializable;
import java.util.ArrayList;

public class Road implements Serializable {
    private ArrayList<ArrayList<Integer>> roadNodeCoordinates;
    private ArrayList<Node> roadNodes;
    private PlayerColour roadColour;
    private ArrayList<ArrayList<Integer>> roadNodeBoardCoordinates;

    public Road(ArrayList<ArrayList<Integer>> roadNodeCoordinates, ArrayList<Node> roadNodes, PlayerColour roadColour, ArrayList<ArrayList<Integer>> roadNodeBoardCoordinates) {
        this.roadNodeCoordinates = roadNodeCoordinates;
        this.roadNodes = roadNodes;
        this.roadColour = roadColour;
        this.roadNodeBoardCoordinates = roadNodeBoardCoordinates;
    }

    public ArrayList<ArrayList<Integer>> getRoadNodeCoordinates() {
        return roadNodeCoordinates;
    }

    public void setRoadNodeCoordinates(ArrayList<ArrayList<Integer>> roadNodeCoordinates) {
        this.roadNodeCoordinates = roadNodeCoordinates;
    }

    public ArrayList<Node> getRoadNodes() {
        return roadNodes;
    }

    public void setRoadNodes(ArrayList<Node> roadNodes) {
        this.roadNodes = roadNodes;
    }

    public PlayerColour getRoadColour() {
        return roadColour;
    }

    public void setRoadColour(PlayerColour roadColour) {
        this.roadColour = roadColour;
    }

    public ArrayList<ArrayList<Integer>> getRoadNodeBoardCoordinates() {
        return roadNodeBoardCoordinates;
    }

    public void setRoadNodeBoardCoordinates(ArrayList<ArrayList<Integer>> roadNodeBoardCoordinates) {
        this.roadNodeBoardCoordinates = roadNodeBoardCoordinates;
    }
}
