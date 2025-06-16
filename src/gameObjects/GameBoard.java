package gameObjects;

import Constants.PlayerColour;
import Constants.ResourceType;

import java.util.*;

public class GameBoard {

    private static GameBoard gameBoard = new GameBoard();
    private static HashMap<ArrayList<Integer>, Tile> tilesDict;
    private static HashMap<ArrayList<Integer>, Node> nodesDict;
    private static HashMap<ArrayList<Integer>, Town> townsDict;
    private static HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;
    private static ArrayList<Player> allPlayers;
    private static int currentPlayerTurn;

    public GameBoard() {
        resetGameBoard();
    }

    public static GameBoard getGameBoard() {
        return gameBoard;
    }

    public static void setGameBoard(GameBoard gameBoard) {
        GameBoard.gameBoard = gameBoard;
    }

    public static HashMap<ArrayList<Integer>, Tile> getTilesDict() {
        return tilesDict;
    }

    public static void setTilesDict(HashMap<ArrayList<Integer>, Tile> tilesDict) {
        GameBoard.tilesDict = tilesDict;
    }

    public static HashMap<ArrayList<Integer>, Node> getNodesDict() {
        return nodesDict;
    }

    public static void setNodesDict(HashMap<ArrayList<Integer>, Node> nodesDict) {
        GameBoard.nodesDict = nodesDict;
    }

    public static HashMap<ArrayList<Integer>, Town> getTownsDict() {
        return townsDict;
    }

    public static void setTownsDict(HashMap<ArrayList<Integer>, Town> townsDict) {
        GameBoard.townsDict = townsDict;
    }

    public static void updatePlayerTownsDict(Town town) {
        townsDict.put(town.getTownCoordinates(), town);
    }

    public static HashMap<ArrayList<ArrayList<Integer>>, Road> getRoadsDict() {
        return roadsDict;
    }

    public static void setRoadsDict(HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict) {
        GameBoard.roadsDict = roadsDict;
    }

    public static void updateRoadsDict(Road newRoad) {
        roadsDict.put(newRoad.getRoadNodeCoordinates(), newRoad);
    }

    public static ArrayList<Player> getAllPlayers() {
        return allPlayers;
    }

    public static void setAllPlayers(ArrayList<Player> allPlayers) {
        GameBoard.allPlayers = allPlayers;
    }

    public static int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public static void setCurrentPlayerTurn(int currentPlayerTurn) {
        GameBoard.currentPlayerTurn = currentPlayerTurn;
    }

    public static void resetGameBoard() {
        nodesDict = new HashMap<>();
        tilesDict = new HashMap<>();
        townsDict = new HashMap<>();
        roadsDict = new HashMap<>();
        allPlayers = new ArrayList<>();
        currentPlayerTurn = 1;

        allPlayers.add(new Player(PlayerColour.RED, 1));
        allPlayers.add(new Player(PlayerColour.BLUE, 2));
        allPlayers.add(new Player(PlayerColour.GREEN, 3));
        allPlayers.add(new Player(PlayerColour.ORANGE, 4));

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
                ArrayList<Integer> correspondingYNodes = switch (y) {
                    case 0 -> new ArrayList<>(Arrays.asList(0, 1, 2, 3));
                    case 1 -> new ArrayList<>(Arrays.asList(2, 3, 4, 5));
                    case 2 -> new ArrayList<>(Arrays.asList(4, 5, 6, 7));
                    case 3 -> new ArrayList<>(Arrays.asList(6, 7, 8, 9));
                    case 4 -> new ArrayList<>(Arrays.asList(8, 9, 10, 11));
                    default -> throw new IllegalStateException("Unexpected value: " + y);
                };
                ArrayList<ArrayList<Integer>> correspondingNodeCoordinates = new ArrayList<>();
                for (int j=correspondingYNodes.getFirst(); j<=correspondingYNodes.getLast(); j++) {
                    if (y == 0 || y == 1) {
                        if (j == correspondingYNodes.getLast())
                            correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x+1, j)));
                        else
                            correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x, j)));
                    }
                    else if (y == 3 || y == 4) {
                        if (j == correspondingYNodes.getFirst())
                            correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x+1, j)));
                        else
                            correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x, j)));
                    }
                    else
                        correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x, j)));

                    if (j != correspondingYNodes.getFirst() && j != correspondingYNodes.getLast())
                        correspondingNodeCoordinates.add(new ArrayList<>(Arrays.asList(x+1, j)));
                }
                tilesDict.put(new ArrayList<>(Arrays.asList(x, y)), new Tile(new ArrayList<>(Arrays.asList(x, y)), resourceChosen, tileDiceNumber, correspondingNodeCoordinates));
            }
        }

        for (int y=0; y<=11; y++) {
            int x_length = switch (y) {
                case 0, 11 -> 3;
                case 1, 2, 9, 10 -> 4;
                case 3, 4, 7, 8 -> 5;
                case 5, 6 -> 6;
                default -> 0;
            };

            for (int x = 0; x < x_length; x++) {
                ArrayList<Tile> connectedTiles = new ArrayList<>();

                for (ArrayList<Integer> tile : tilesDict.keySet()) {
                    if (tilesDict.get(tile).getCorrespondingNodeCoordinates().contains(Arrays.asList(x, y)))
                        connectedTiles.add(tilesDict.get(Arrays.asList(tile.getFirst(), tile.getLast())));

                    nodesDict.put(new ArrayList<>(Arrays.asList(x, y)), new Node(new ArrayList<Integer>(Arrays.asList(x, y)), new ArrayList<Node>(), connectedTiles));
                }
            }
        }
        for (int y=0; y<=11; y++) {
            int x_length = switch (y) {
                case 0, 11 -> 3;
                case 1, 2, 9, 10 -> 4;
                case 3, 4, 7, 8 -> 5;
                case 5, 6 -> 6;
                default -> 0;
            };

            for (int x = 0; x < x_length; x++) {
                ArrayList<Node> connectedNodes;
                switch (y) {
                    case 0:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y+1)));
                        if (nodesDict.containsKey(Arrays.asList(x+1, y+1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x+1, y+1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                    case 1, 3, 5:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y+1)));
                        if (nodesDict.containsKey(Arrays.asList(x, y-1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x, y-1)));
                        if (nodesDict.containsKey(Arrays.asList(x-1, y-1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x-1, y-1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                    case 7, 9:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y+1)));
                        if (nodesDict.containsKey(Arrays.asList(x, y-1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x, y-1)));
                        if (nodesDict.containsKey(Arrays.asList(x+1, y-1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x+1, y-1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                    case 2, 4:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y-1)));
                        if (nodesDict.containsKey(Arrays.asList(x, y+1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x, y+1)));
                        if (nodesDict.containsKey(Arrays.asList(x+1, y+1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x+1, y+1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                    case 6, 8, 10:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y-1)));
                        if (nodesDict.containsKey(Arrays.asList(x, y+1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x, y+1)));
                        if (nodesDict.containsKey(Arrays.asList(x-1, y+1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x-1, y+1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                    default:
                        connectedNodes = new ArrayList<>();
                        connectedNodes.add(nodesDict.get(Arrays.asList(x, y-1)));
                        if (nodesDict.containsKey(Arrays.asList(x+1, y-1)))
                            connectedNodes.add(nodesDict.get(Arrays.asList(x+1, y-1)));
                        nodesDict.get(Arrays.asList(x, y)).setConnectedNodes(connectedNodes);
                        break;
                }
            }
        }
    }
}
