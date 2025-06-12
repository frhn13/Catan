package gameObjects;

import Constants.PlayerColour;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {
    private PlayerColour playerColour;
    private int score;
    private HashMap<ArrayList<Integer>, Town> playerTownsDict;
    private HashMap<ArrayList<ArrayList<Integer>>, Road> playerRoadsDict;

    public Player(PlayerColour playerColour) {
        this.playerColour = playerColour;
        this.score = 0;
        this.playerTownsDict = new HashMap<>();
        this.playerRoadsDict = new HashMap<>();
    }
}
