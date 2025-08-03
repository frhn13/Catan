package gameObjects;

import Constants.PlayerColour;
import Constants.ResourceType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Player implements Serializable {
    private PlayerColour playerColour;
    private int playerNumber;
    private int score;
    private HashMap<ArrayList<Integer>, Town> playerTownsDict;
    private HashMap<ArrayList<ArrayList<Integer>>, Road> playerRoadsDict;
    private HashMap<ResourceType, Integer> playerResourcesDict;
    private int initialPlacements;

    public Player(PlayerColour playerColour, int playerNumber) {
        this.playerColour = playerColour;
        this.playerNumber = playerNumber;
        this.score = 0;
        this.playerTownsDict = new HashMap<>();
        this.playerRoadsDict = new HashMap<>();
        this.playerResourcesDict = new HashMap<>();
        this.initialPlacements = 0;

        this.playerResourcesDict.put(ResourceType.WOOL, 0);
        this.playerResourcesDict.put(ResourceType.GRAIN, 0);
        this.playerResourcesDict.put(ResourceType.ORE, 0);
        this.playerResourcesDict.put(ResourceType.LUMBER, 0);
        this.playerResourcesDict.put(ResourceType.BRICK, 0);
    }

    public Player() {
        this.score = 0;
        this.playerTownsDict = new HashMap<>();
        this.playerRoadsDict = new HashMap<>();
        this.playerResourcesDict = new HashMap<>();
        this.initialPlacements = 0;

        this.playerResourcesDict.put(ResourceType.WOOL, 20);
        this.playerResourcesDict.put(ResourceType.GRAIN, 20);
        this.playerResourcesDict.put(ResourceType.ORE, 20);
        this.playerResourcesDict.put(ResourceType.LUMBER, 20);
        this.playerResourcesDict.put(ResourceType.BRICK, 20);
    }

    public PlayerColour getPlayerColour() {
        return playerColour;
    }

    public void setPlayerColour(PlayerColour playerColour) {
        this.playerColour = playerColour;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public HashMap<ArrayList<Integer>, Town> getPlayerTownsDict() {
        return playerTownsDict;
    }

    public void setPlayerTownsDict(HashMap<ArrayList<Integer>, Town> playerTownsDict) {
        this.playerTownsDict = playerTownsDict;
    }

    public void updatePlayerTownsDict(Town newTown) {
        this.playerTownsDict.put(newTown.getTownCoordinates(), newTown);
    }

    public HashMap<ArrayList<ArrayList<Integer>>, Road> getPlayerRoadsDict() {
        return playerRoadsDict;
    }

    public void setPlayerRoadsDict(HashMap<ArrayList<ArrayList<Integer>>, Road> playerRoadsDict) {
        this.playerRoadsDict = playerRoadsDict;
    }

    public void updatePlayerRoadsDict(Road newRoad) {
        this.playerRoadsDict.put(newRoad.getRoadNodeCoordinates(), newRoad);
    }

    public HashMap<ResourceType, Integer> getPlayerResourcesDict() {
        return playerResourcesDict;
    }

    public void setPlayerResourcesDict(HashMap<ResourceType, Integer> playerResourcesDict) {
        this.playerResourcesDict = playerResourcesDict;
    }

    public void updatePlayerResourcesDict(HashMap<ResourceType, Integer> newResources) {
        for (ResourceType newResourceType : newResources.keySet()) {
            for (ResourceType currentResourceType : this.playerResourcesDict.keySet()) {
                if (newResourceType == currentResourceType)
                    this.playerResourcesDict.put(currentResourceType, this.playerResourcesDict.get(currentResourceType) + newResources.get(newResourceType));
            }
        }
    }

    public int getInitialPlacements() {
        return initialPlacements;
    }

    public void setInitialPlacements(int initialPlacements) {
        this.initialPlacements = initialPlacements;
    }
}
