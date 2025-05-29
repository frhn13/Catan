package gameObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameBoard {

    private static GameBoard gameBoard = new GameBoard();
    public static ArrayList<Tile> tiles;

    public GameBoard() {
        ArrayList<Tile> tiles = new ArrayList<>();

        ResourceType[] resourceTypes = new ResourceType[] {ResourceType.LUMBER,
                ResourceType.GRAIN, ResourceType.WOOL, ResourceType.BRICK, ResourceType.ORE, ResourceType.DESERT};

        HashMap<ResourceType, Integer> resourceNumbers = new HashMap<>();

        resourceNumbers.put(ResourceType.LUMBER, 4);
        resourceNumbers.put(ResourceType.GRAIN, 4);
        resourceNumbers.put(ResourceType.WOOL, 4);
        resourceNumbers.put(ResourceType.BRICK, 3);
        resourceNumbers.put(ResourceType.ORE, 3);
        resourceNumbers.put(ResourceType.DESERT, 1);

        HashMap<Integer, Integer> diceValueNumbers = new HashMap<>();

        diceValueNumbers.put(2, 1);
        diceValueNumbers.put(3, 2);
        diceValueNumbers.put(4, 2);
        diceValueNumbers.put(5, 2);
        diceValueNumbers.put(6, 2);
        diceValueNumbers.put(8, 2);
        diceValueNumbers.put(9, 2);
        diceValueNumbers.put(10, 2);
        diceValueNumbers.put(11, 2);
        diceValueNumbers.put(12, 1);

        for (int y=0; y<=4; y++) {
            int x_length = switch (y) {
                case 0, 4 -> 3;
                case 1, 3 -> 4;
                case 2 -> 5;
                default -> 0;
            };

            for (int x=0; x<x_length; x++) {
                ResourceType resourceChosen;
                int tileDiceNumber;
                while (true) {
                    int diceValueNumber = (int) (Math.random() * 11) + 2;
                    int randomResourceNumber = (int) (Math.random() * 6);
                    resourceChosen = resourceTypes[randomResourceNumber];

                    if (resourceNumbers.get(resourceChosen) > 0 && diceValueNumber != 7 && (diceValueNumbers.get(diceValueNumber) > 0 || resourceChosen == ResourceType.DESERT)) {
                        resourceNumbers.replace(resourceChosen, resourceNumbers.get(resourceChosen)-1);
                        if (resourceChosen == ResourceType.DESERT) {
                            tileDiceNumber = 0;
                        } else {
                            diceValueNumbers.replace(diceValueNumber, diceValueNumbers.get(diceValueNumber)-1);
                            tileDiceNumber = diceValueNumber;
                        }
                        break;
                    }
                }
                tiles.add(new Tile(new ArrayList<>(Arrays.asList(x, y)), resourceChosen, tileDiceNumber));
            }
        }

        GameBoard.tiles = tiles;
    }

    public static GameBoard getGameBoard() {
        return gameBoard;
    }

    public static void setGameBoard(GameBoard gameBoard) {
        GameBoard.gameBoard = gameBoard;
    }

    public static ArrayList<Tile> getTiles() {
        return tiles;
    }

}
