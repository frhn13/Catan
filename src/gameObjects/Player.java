package gameObjects;

import Constants.PlayerColour;
import Constants.ResourceType;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {
    private PlayerColour playerColour;
    private int playerNumber;
    private int score;
    private HashMap<ArrayList<Integer>, Town> playerTownsDict;
    private HashMap<ArrayList<ArrayList<Integer>>, Road> playerRoadsDict;
    private HashMap<ResourceType, Integer> playerResourcesDict;

    public Player(PlayerColour playerColour, int playerNumber) {
        this.playerColour = playerColour;
        this.playerNumber = playerNumber;
        this.score = 0;
        this.playerTownsDict = new HashMap<>();
        this.playerRoadsDict = new HashMap<>();
        this.playerResourcesDict = new HashMap<>();

        this.playerResourcesDict.put(ResourceType.WOOL, 0);
        this.playerResourcesDict.put(ResourceType.GRAIN, 0);
        this.playerResourcesDict.put(ResourceType.ORE, 0);
        this.playerResourcesDict.put(ResourceType.LUMBER, 0);
        this.playerResourcesDict.put(ResourceType.BRICK, 0);
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

    public HashMap<ArrayList<ArrayList<Integer>>, Road> getPlayerRoadsDict() {
        return playerRoadsDict;
    }

    public void setPlayerRoadsDict(HashMap<ArrayList<ArrayList<Integer>>, Road> playerRoadsDict) {
        this.playerRoadsDict = playerRoadsDict;
    }

    public HashMap<ResourceType, Integer> getPlayerResourcesDict() {
        return playerResourcesDict;
    }

    public void setPlayerResourcesDict(HashMap<ResourceType, Integer> playerResourcesDict) {
        this.playerResourcesDict = playerResourcesDict;
    }

    public void updatePlayerResourcesDict(ResourceType resource, int amount) {
        this.playerResourcesDict.put(resource, this.playerResourcesDict.get(resource) + amount);
    }
}
