package gameVisualisation;

import Constants.GameState;
import Constants.PlayerColour;
import Constants.ResourceType;
import Constants.SCMessages;
import gameObjects.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

import static Constants.CSMessages.*;
import static Constants.Constants.*;


public class MainGame extends JFrame implements ActionListener, MouseListener {
    JPanel gamePanel;
    JButton rollDiceButton;
    JButton buildRoadButton;
    JButton buildSettlementButton;
    JButton upgradeSettlementButton;
    JButton endTurnButton;
    JLabel diceRollLabel;

    JLabel thisPlayerScoreLabel = new JLabel();
    JLabel thisPlayerLabel;
    JLabel player1Label = new JLabel();
    JLabel player2Label = new JLabel();
    JLabel player3Label = new JLabel();
    JLabel currentPlayerLabel = new JLabel();

    JPanel endgamePanel;
    JLabel scoresLabel;
    JLabel numberOfSettlementsLabel;
    JLabel numberOfCitiesLabel;
    JButton newGameButton;

    int diceValue;
    boolean buildingNewSettlement = false;
    boolean upgradingToCity = false;
    boolean buildingNewRoad = false;

    StringBuilder finalScores = new StringBuilder();
    StringBuilder finalSettlements = new StringBuilder();
    StringBuilder finalCities = new StringBuilder();

    Player player;
    GameClient gameClient = new GameClient();

    ArrayList<Player> players = GameBoard.getAllPlayers();
    ArrayList<Player> allPlayers;
    HashMap<ArrayList<Integer>, Tile> tilesDict;
    HashMap<ArrayList<Integer>, Node> nodesDict;
    HashMap<ArrayList<Integer>, Town> townsDict;
    HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;
    int currentPlayerTurn;

    GameState gameState = GameState.INITIAL_PLACEMENT;

    public MainGame() {
        gameClient.connectToServer();

        System.out.println("Connected to server as Player #"+player.getPlayerNumber()+" with colour "+player.getPlayerColour()+".");
        // Post page panel code
        endgamePanel = new JPanel();

        scoresLabel = new JLabel(String.valueOf(finalScores)) {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 8, 100, 800, 400);
            }
        };

        numberOfSettlementsLabel = new JLabel(String.valueOf(finalSettlements)) {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 10, 300, 1000, 400);
            }
        };

        numberOfCitiesLabel = new JLabel(String.valueOf(finalCities)) {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 10, 500, 1000, 400);
            }
        };

        scoresLabel.setFont(SCORE_FONT);
        numberOfSettlementsLabel.setFont(SCORE_FONT);
        numberOfCitiesLabel.setFont(SCORE_FONT);

        newGameButton = new JButton("Start New Game") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 2 - 100, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameClient.resetGame();

                buildingNewSettlement = false;
                upgradingToCity = false;
                buildingNewRoad = false;

                players = GameBoard.getAllPlayers();
                tilesDict = GameBoard.getTilesDict();
                nodesDict = GameBoard.getNodesDict();
                townsDict = GameBoard.getTownsDict();
                roadsDict = GameBoard.getRoadsDict();

                gameState = GameState.INITIAL_PLACEMENT;
//                thisPlayerScoreLabel.setText("Their Score: " + player.getScore());
//                thisPlayerLabel.setText(player.getPlayerColour() + "'s Turn");
                diceRollLabel.setVisible(false);
                startOfNewGame();
            }
        });

        endgamePanel.add(scoresLabel);
        endgamePanel.add(numberOfSettlementsLabel);
        endgamePanel.add(numberOfCitiesLabel);
        endgamePanel.add(newGameButton);

        // Code for panel during the game
        gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                int card_num = 0;
                for (ResourceType resource : player.getPlayerResourcesDict().keySet()) {
                    if (player.getPlayerResourcesDict().get(resource) > 0) {
                        Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                        g.drawImage(cardImg, card_num * 100 + 50, DEFAULT_GAME_HEIGHT - 130, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(new Font("Arial", Font.BOLD, 30));
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(player.getPlayerResourcesDict().get(resource)), card_num * 100 + 70, DEFAULT_GAME_HEIGHT - 90);
                        card_num++;
                    }
                }

                int base_x;
                for (ArrayList<Integer> tile : tilesDict.keySet()) {
                    base_x = switch (tilesDict.get(tile).getTileCoordinates().getLast()) {
                        case 0, 4 -> 300;
                        case 1, 3 -> 200;
                        default -> 100;
                    };
                    try {
                        Image tileImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameTiles/" + tilesDict.get(tile).getTileResource().tileImage))).getImage();
                        g.drawImage(tileImg, tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 150 + 50, TILE_WIDTH, TILE_HEIGHT, null);
                        g.setFont(new Font("Arial", Font.BOLD, 50));
                        g.setColor(Color.white);
                        g.drawString(String.valueOf(tilesDict.get(tile).getRollValue()), tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x + 80, tilesDict.get(tile).getTileCoordinates().getLast() * 150 + 150);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (ArrayList<Integer> node : nodesDict.keySet()) {
                    base_x = switch (nodesDict.get(node).getNodeCoordinates().getLast()) {
                        case 0, 11 -> 400;
                        case 1, 2, 9, 10 -> 300;
                        case 3, 4, 7, 8 -> 200;
                        default -> 100;
                    };
                    int y_pos = switch (nodesDict.get(node).getNodeCoordinates().getLast()) {
                        case 0 -> 50;
                        case 1 -> 100;
                        case 2 -> 200;
                        case 3 -> 250;
                        case 4 -> 350;
                        case 5 -> 400;
                        case 6 -> 500;
                        case 7 -> 550;
                        case 8 -> 650;
                        case 9 -> 700;
                        case 10 -> 800;
                        default -> 850;
                    };
                    g.setColor(Color.BLACK);
                    g.fillOval(nodesDict.get(node).getNodeCoordinates().getFirst() * 200 + base_x - 10, y_pos - 10, 20, 20);
                    nodesDict.get(node).setNodeBoardCoordinates(new ArrayList<>(Arrays.asList(nodesDict.get(node).getNodeCoordinates().getFirst() * 200 + base_x - 10, y_pos - 10)));
                }

                for (ArrayList<Integer> town : townsDict.keySet()) {
                    g.setColor(townsDict.get(town).getTownColour().colour);
                    if (townsDict.get(town).isCity())
                        g.fillRect(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 20, 20);
                    else
                        g.fillOval(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 20, 20);
                }

                for (ArrayList<ArrayList<Integer>> road : roadsDict.keySet()) {
                    System.out.println(road.getFirst() + " " + road.getLast());
                    g.setColor(roadsDict.get(road).getRoadColour().colour);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(roadsDict.get(road).getRoadNodeBoardCoordinates().getFirst().getFirst() + 10,
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getFirst().getLast(),
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getLast().getFirst() + 10,
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getLast().getLast());
                }
            }
        };

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                boolean neighbouring_settlement = false;
                if (buildingNewSettlement) {
                    System.out.println(mouseX);
                    System.out.println(mouseY);

                    for (ArrayList<Integer> node : nodesDict.keySet()) {
                        if (mouseX >= nodesDict.get(node).getNodeBoardCoordinates().getFirst() &&
                                mouseX <= nodesDict.get(node).getNodeBoardCoordinates().getFirst() + 20 &&
                                mouseY >= nodesDict.get(node).getNodeBoardCoordinates().getLast() &&
                                mouseY <= nodesDict.get(node).getNodeBoardCoordinates().getLast() + 20 &&
                                !nodesDict.get(node).isHasSettlement()) {

                            for (Node neighbourNode : nodesDict.get(node).getConnectedNodes()) {
                                if (neighbourNode.isHasSettlement()) {
                                    neighbouring_settlement = true;
                                    break;
                                }
                            }

                            if (!neighbouring_settlement) {
                                if (gameState == GameState.INITIAL_PLACEMENT) {
                                    // Player currentPlayer = findCurrentPlayer();
                                    Town newTown = new Town(nodesDict.get(node).getNodeCoordinates(), nodesDict.get(node).getConnectedNodes(),
                                            nodesDict.get(node).getConnectedTiles(), player.getPlayerColour(), nodesDict.get(node).getNodeBoardCoordinates());
                                    player.updatePlayerTownsDict(newTown);

                                    player.setScore(player.getScore() + 1);

                                    player.setInitialPlacements(player.getInitialPlacements() + 1);
                                    buildingNewSettlement = false;
                                    nodesDict.get(node).setHasSettlement(true);
                                    townsDict.put(newTown.getTownCoordinates(), newTown);

                                    if (currentPlayerTurn == player.getPlayerNumber()) {
                                        buildSettlementButton.setVisible(false);
                                        buildRoadButton.setVisible(true);
                                    }

                                    if (player.getInitialPlacements() == TOTAL_INITIAL_PLACEMENTS) {
                                        ArrayList<Tile> tiles = nodesDict.get(node).getConnectedTiles();
                                        HashMap<ResourceType, Integer> newResources = new HashMap<>();
                                        for (Tile tile : tiles) {
                                            if (newResources.containsKey(tile.getTileResource()))
                                                newResources.put(tile.getTileResource(), newResources.get(tile.getTileResource()) + 1);
                                            else
                                                newResources.put(tile.getTileResource(), 1);
                                        }
                                        player.updatePlayerResourcesDict(newResources);
                                    }
                                    gameClient.addSettlement();
                                    break;
                                } else if (gameState == GameState.NORMAL_PLAY) {
                                    // Player currentPlayer = findCurrentPlayer();
                                    outerLoop:
                                    for (ArrayList<ArrayList<Integer>> roads : player.getPlayerRoadsDict().keySet()) {
                                        for (ArrayList<Integer> roadNode : roads) {
                                            if (roadNode.equals(node)) {
                                                Town newTown = new Town(nodesDict.get(node).getNodeCoordinates(), nodesDict.get(node).getConnectedNodes(),
                                                        nodesDict.get(node).getConnectedTiles(), player.getPlayerColour(), nodesDict.get(node).getNodeBoardCoordinates());
                                                player.updatePlayerTownsDict(newTown);
                                                player.setScore(player.getScore() + 1);

                                                HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                                removedResources.put(ResourceType.LUMBER, -1);
                                                removedResources.put(ResourceType.BRICK, -1);
                                                removedResources.put(ResourceType.GRAIN, -1);
                                                removedResources.put(ResourceType.WOOL, -1);

                                                player.updatePlayerResourcesDict(removedResources);
                                                player.setInitialPlacements(player.getInitialPlacements() + 1);
                                                buildingNewSettlement = false;
                                                nodesDict.get(node).setHasSettlement(true);
                                                townsDict.put(newTown.getTownCoordinates(), newTown);
                                                gameClient.addSettlement();

                                                break outerLoop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (upgradingToCity && gameState == GameState.NORMAL_PLAY) {
                    System.out.println(mouseX);
                    System.out.println(mouseY);
                    Player currentPlayer = findCurrentPlayer();
                    HashMap<ArrayList<Integer>, Town> playerTownsDict = player.getPlayerTownsDict();
                    for (ArrayList<Integer> town : playerTownsDict.keySet()) {
                        if (mouseX >= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() &&
                                mouseX <= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() + 20 &&
                                mouseY >= playerTownsDict.get(town).getTownBoardCoordinates().getLast() &&
                                mouseY <= playerTownsDict.get(town).getTownBoardCoordinates().getLast() + 20 &&
                                !playerTownsDict.get(town).isCity()) {
                            //System.out.println(playerTownsDict.get(town).getTownCoordinates());
                            townsDict.get(town).setCity(true);
                            playerTownsDict.get(town).setCity(true);
                            player.updatePlayerTownsDict(playerTownsDict.get(town));
                            player.setScore(player.getScore() + 1);

                            HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                            removedResources.put(ResourceType.GRAIN, -2);
                            removedResources.put(ResourceType.ORE, -3);
                            player.updatePlayerResourcesDict(removedResources);
                            upgradingToCity = false;
                            gameClient.upgradeToCity();
                            break;
                        }
                    }
                }

                if (buildingNewRoad) {
                    System.out.println(mouseX);
                    System.out.println(mouseY);
                    Player currentPlayer = findCurrentPlayer();

                    if (gameState == GameState.NORMAL_PLAY) {
                        outerLoop:
                        for (ArrayList<ArrayList<Integer>> road : player.getPlayerRoadsDict().keySet()) {
                            for (ArrayList<Integer> node : road) {
                                for (Node neighbourNode : nodesDict.get(node).getConnectedNodes()) {
                                    ArrayList<Integer> startNodeCoordinates = nodesDict.get(node).getNodeBoardCoordinates();
                                    ArrayList<Integer> endNodeCoordinates = neighbourNode.getNodeBoardCoordinates();

                                    if (((startNodeCoordinates.getFirst() < endNodeCoordinates.getFirst() &&
                                            mouseX <= endNodeCoordinates.getFirst() && mouseX >= startNodeCoordinates.getFirst())
                                            || (startNodeCoordinates.getFirst() > endNodeCoordinates.getFirst() &&
                                            mouseX >= endNodeCoordinates.getFirst() && mouseX <= startNodeCoordinates.getFirst())
                                            || (Objects.equals(startNodeCoordinates.getFirst(), endNodeCoordinates.getFirst()) &&
                                            mouseX >= startNodeCoordinates.getFirst() && mouseX <= startNodeCoordinates.getFirst() + 20)) &&
                                            ((startNodeCoordinates.getLast() <= endNodeCoordinates.getLast() &&
                                                    mouseY <= endNodeCoordinates.getLast() && mouseY >= startNodeCoordinates.getLast())
                                                    || (startNodeCoordinates.getLast() > endNodeCoordinates.getLast() &&
                                                    mouseY >= endNodeCoordinates.getLast() && mouseY <= startNodeCoordinates.getLast())) &&
                                            !GameBoard.getRoadsDict().containsKey(new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(nodesDict.get(node).getNodeCoordinates().getFirst(), nodesDict.get(node).getNodeCoordinates().getLast())),
                                                    new ArrayList<>(Arrays.asList(neighbourNode.getNodeCoordinates().getFirst(), neighbourNode.getNodeCoordinates().getLast())))))) {

                                        currentPlayer = findCurrentPlayer();
                                        Road newRoad = new Road(new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(nodesDict.get(node).getNodeCoordinates().getFirst(), nodesDict.get(node).getNodeCoordinates().getLast())),
                                                new ArrayList<>(Arrays.asList(neighbourNode.getNodeCoordinates().getFirst(), neighbourNode.getNodeCoordinates().getLast())))),
                                                new ArrayList<>(Arrays.asList(nodesDict.get(node), neighbourNode)),
                                                player.getPlayerColour(),
                                                new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(startNodeCoordinates.getFirst(), startNodeCoordinates.getLast())),
                                                        new ArrayList<>(Arrays.asList(endNodeCoordinates.getFirst(), endNodeCoordinates.getLast())))));
                                        player.updatePlayerRoadsDict(newRoad);
                                        roadsDict.put(newRoad.getRoadNodeCoordinates(), newRoad);

                                        HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                        removedResources.put(ResourceType.LUMBER, -1);
                                        removedResources.put(ResourceType.BRICK, -1);

                                        player.updatePlayerResourcesDict(removedResources);
                                        buildingNewRoad = false;
                                        gameClient.addRoad();
                                        break outerLoop;
                                    }
                                }
                            }
                        }
                    }

                    outerLoop:
                    for (ArrayList<Integer> town : player.getPlayerTownsDict().keySet()) {
                        for (Node neighbourNode : player.getPlayerTownsDict().get(town).getConnectedNodes()) {
                            ArrayList<Integer> townCoordinates = player.getPlayerTownsDict().get(town).getTownBoardCoordinates();
                            ArrayList<Integer> nodeCoordinates = neighbourNode.getNodeBoardCoordinates();

                            if (((townCoordinates.getFirst() < nodeCoordinates.getFirst() &&
                                    mouseX <= nodeCoordinates.getFirst() && mouseX >= townCoordinates.getFirst())
                            || (townCoordinates.getFirst() > nodeCoordinates.getFirst() &&
                                    mouseX >= nodeCoordinates.getFirst() && mouseX <= townCoordinates.getFirst())
                            || (Objects.equals(townCoordinates.getFirst(), nodeCoordinates.getFirst()) &&
                                    mouseX >= townCoordinates.getFirst() && mouseX <= townCoordinates.getFirst() + 20)) &&
                            ((townCoordinates.getLast() <= nodeCoordinates.getLast() &&
                                    mouseY <= nodeCoordinates.getLast() && mouseY >= townCoordinates.getLast())
                            || (townCoordinates.getLast() > nodeCoordinates.getLast() &&
                                    mouseY >= nodeCoordinates.getLast() && mouseY <= townCoordinates.getLast())) &&
                            !GameBoard.getRoadsDict().containsKey(new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(townsDict.get(town).getTownCoordinates().getFirst(), townsDict.get(town).getTownCoordinates().getLast())),
                                    new ArrayList<>(Arrays.asList(neighbourNode.getNodeCoordinates().getFirst(), neighbourNode.getNodeCoordinates().getLast())))))) {

                                currentPlayer = findCurrentPlayer();
                                Road newRoad = new Road(new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(townsDict.get(town).getTownCoordinates().getFirst(), townsDict.get(town).getTownCoordinates().getLast())),
                                            new ArrayList<>(Arrays.asList(neighbourNode.getNodeCoordinates().getFirst(), neighbourNode.getNodeCoordinates().getLast())))),
                                        new ArrayList<>(Arrays.asList(nodesDict.get(town), neighbourNode)),
                                        player.getPlayerColour(),
                                        new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(townCoordinates.getFirst(), townCoordinates.getLast())),
                                            new ArrayList<>(Arrays.asList(nodeCoordinates.getFirst(), nodeCoordinates.getLast())))));
                                player.updatePlayerRoadsDict(newRoad);
                                roadsDict.put(newRoad.getRoadNodeCoordinates(), newRoad);

                                if (gameState == GameState.NORMAL_PLAY) {
                                    HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                    removedResources.put(ResourceType.LUMBER, -1);
                                    removedResources.put(ResourceType.BRICK, -1);
                                    player.updatePlayerResourcesDict(removedResources);
                                }

                                gameClient.addRoad();

                                if (gameState == GameState.INITIAL_PLACEMENT) {
                                    if (player.getInitialPlacements() >= 2 && player.getPlayerNumber() == 4)
                                        gameClient.startNormalGame();
                                    currentPlayerTurn = player.getPlayerNumber() < 4 ? player.getPlayerNumber() + 1 : 1;
                                    buildRoadButton.setVisible(false);
                                    buildSettlementButton.setVisible(false);
                                    gameClient.updateTurn();
                                }

                                buildingNewRoad = false;
                                break outerLoop;
                            }
                        }
                    }
                }
                Player currentPlayer = findCurrentPlayer();

                System.out.println("BRICK: " + player.getPlayerResourcesDict().get(ResourceType.BRICK));
                System.out.println("LUMBER: " + player.getPlayerResourcesDict().get(ResourceType.LUMBER));
                System.out.println("GRAIN: " + player.getPlayerResourcesDict().get(ResourceType.GRAIN));
                System.out.println("WOOL: " + player.getPlayerResourcesDict().get(ResourceType.WOOL));
                System.out.println("ORE: " + player.getPlayerResourcesDict().get(ResourceType.ORE));
                System.out.println(gameState);

                if (player.getScore() >= WINNING_SCORE) {
                    gameState = GameState.ENDGAME;
                    gameClient.endGame();
                }
                System.out.println();
            }
        });

        Player currentPlayer = findCurrentPlayer();

        rollDiceButton = new JButton("Roll Dice") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 2 - 100, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        buildRoadButton = new JButton("Build Road") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(100, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        buildSettlementButton = new JButton("Build Settlement") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(350, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        upgradeSettlementButton = new JButton("Upgrade to City") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(600, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        endTurnButton = new JButton("End Turn") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        diceRollLabel = new JLabel() {
          public void setBounds(int x, int y, int width, int height) {
              super.setBounds(DEFAULT_GAME_WIDTH / 2 - 50, DEFAULT_GAME_HEIGHT - 300, 100, 100);
          }
        };

        thisPlayerScoreLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 300, 300, 100);
            }
        };

        thisPlayerLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 350, 400, 100);
            }
        };

        currentPlayerLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds((int) DEFAULT_GAME_WIDTH / 2 - 200, DEFAULT_GAME_HEIGHT - 350, 400, 100);
            }
        };

        player1Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 300, DEFAULT_GAME_HEIGHT - 400, 300, 100);
            }
        };

        player2Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 300, DEFAULT_GAME_HEIGHT - 300, 300, 100);
            }
        };

        player3Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 300, DEFAULT_GAME_HEIGHT - 200, 300, 100);
            }
        };

        if (gameState == GameState.INITIAL_PLACEMENT || gameState == GameState.LOBBY) {
            buildSettlementButton.setVisible(false);
            buildRoadButton.setVisible(false);
            rollDiceButton.setVisible(false);
            upgradeSettlementButton.setVisible(false);
            endTurnButton.setVisible(false);
            diceRollLabel.setVisible(false);
            diceRollLabel.setFont(DICE_ROLL_FONT);
            thisPlayerScoreLabel.setFont(SCORE_FONT);
            thisPlayerLabel.setFont(SCORE_FONT);
            currentPlayerLabel.setFont(SCORE_FONT);
            player1Label.setFont(OTHER_SCORE_FONT);
            player2Label.setFont(OTHER_SCORE_FONT);
            player3Label.setFont(OTHER_SCORE_FONT);
            if (currentPlayerTurn == player.getPlayerNumber()) {
                buildSettlementButton.setVisible(true);
            }
        }

        rollDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                upgradingToCity = false;
                buildingNewRoad = false;
                Random random = new Random();
                int diceRoll1 = random.nextInt(6) + 1;
                int diceRoll2 = random.nextInt(6) + 1;
                int diceValue = diceRoll1 + diceRoll2;

                Player currentPlayer = findCurrentPlayer();
                HashMap<ArrayList<Integer>, Town> playerTownsDict = player.getPlayerTownsDict();

                diceRollLabel.setVisible(true);
                diceRollLabel.setText(String.valueOf(diceValue));

                HashMap<ResourceType, Integer> newResources = new HashMap<>();
                for (Tile tile : tilesDict.values()) {
                    for (ArrayList<Integer> tileNode : tile.getCorrespondingNodeCoordinates()) {
                        for (ArrayList<Integer> town : playerTownsDict.keySet()) {
                            if (playerTownsDict.get(town).getTownCoordinates().equals(tileNode) &&
                                    tile.getRollValue() == diceValue) {
                                int resourceAmount = playerTownsDict.get(town).isCity() ? 2 : 1;
                                if (newResources.containsKey(tile.getTileResource()))
                                    newResources.put(tile.getTileResource(), newResources.get(tile.getTileResource()) + resourceAmount);
                                else
                                    newResources.put(tile.getTileResource(), resourceAmount);
                            }
                        }
                    }
                }

                if (player.getPlayerNumber() == currentPlayerTurn) {
                    rollDiceButton.setVisible(false);
                    buildRoadButton.setVisible(true);
                    buildSettlementButton.setVisible(true);
                    upgradeSettlementButton.setVisible(true);
                    endTurnButton.setVisible(true);

                    player.updatePlayerResourcesDict(newResources);
                    gameClient.updateResources();
                    gamePanel.repaint();
                }
            }
        });

        buildRoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                upgradingToCity = false;
                Player currentPlayer = findCurrentPlayer();
                if ((player.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
                    buildingNewRoad = !buildingNewRoad;
                }
                System.out.println(buildingNewRoad);
            }
        });

        buildSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upgradingToCity = false;
                buildingNewRoad = false;
                Player currentPlayer = findCurrentPlayer();
                if ((player.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.WOOL) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
                    buildingNewSettlement = !buildingNewSettlement;
                }
                System.out.println(buildingNewSettlement);
            }
        });

        upgradeSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                buildingNewRoad = false;
                Player currentPlayer = findCurrentPlayer();
                if (player.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 2 &&
                player.getPlayerResourcesDict().get(ResourceType.ORE) >= 3) {
                    upgradingToCity = !upgradingToCity;
                }
                System.out.println(upgradingToCity);
            }
        });

        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                buildingNewRoad = false;
                upgradingToCity = false;

                rollDiceButton.setVisible(false);
                buildRoadButton.setVisible(false);
                buildSettlementButton.setVisible(false);
                upgradeSettlementButton.setVisible(false);
                endTurnButton.setVisible(false);

                Player currentPlayer = findCurrentPlayer();
                currentPlayerTurn = player.getPlayerNumber() < 4 ? player.getPlayerNumber() + 1 : 1;
                gameClient.updateTurn();
                gamePanel.repaint();
            }
        });

        gamePanel.add(rollDiceButton);
        gamePanel.add(buildRoadButton);
        gamePanel.add(buildSettlementButton);
        gamePanel.add(upgradeSettlementButton);
        gamePanel.add(endTurnButton);
        gamePanel.add(diceRollLabel);
        gamePanel.add(thisPlayerScoreLabel);
        gamePanel.add(thisPlayerLabel);
        gamePanel.add(currentPlayerLabel);
        gamePanel.add(player1Label);
        gamePanel.add(player2Label);
        gamePanel.add(player3Label);

        this.setSize(DEFAULT_GAME_WIDTH, DEFAULT_GAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        if (gameState == GameState.INITIAL_PLACEMENT || gameState == GameState.LOBBY) {
            this.add(gamePanel);
            this.remove(endgamePanel);
            this.repaint();
        }
        else if (gameState == GameState.ENDGAME) {
            this.add(endgamePanel);
            this.remove(gamePanel);
            this.repaint();
        }

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public Player findCurrentPlayer() {
        for (Player currentPlayer : players) {
            if (currentPlayer.getPlayerNumber() == GameBoard.getCurrentPlayerTurn())
                return currentPlayer;
        }
        return null;
    }

    public void endOfGame() {
        finalScores.append("Final Score: ");
        finalSettlements.append("Number of Settlements: ");
        finalCities.append("Number of Cities: ");
        for (Player currentPlayer : allPlayers) {
            finalScores.append(currentPlayer.getPlayerColour()).append(": ").append(currentPlayer.getScore()).append(" ");
            int noSettlements = 0;
            int noCities = 0;
            for (Town town : currentPlayer.getPlayerTownsDict().values()) {
                if (town.isCity()) noCities++;
                else noSettlements++;
            }
            finalSettlements.append(currentPlayer.getPlayerColour()).append(": ").append(noSettlements).append(" ");
            finalCities.append(currentPlayer.getPlayerColour()).append(": ").append(noCities).append(" ");
        }

        scoresLabel.setText(String.valueOf(finalScores));
        numberOfSettlementsLabel.setText(String.valueOf(finalSettlements));
        numberOfCitiesLabel.setText(String.valueOf(finalCities));

        this.add(endgamePanel);
        this.remove(gamePanel);
        this.repaint();
    }

    public void startOfNewGame() {
        this.add(gamePanel);
        this.remove(endgamePanel);
        this.repaint();
    }

    public class GameClient {

        private ClientSideConnection csc;

        public void connectToServer() {
            csc = new ClientSideConnection();
            int playerID = csc.playerID;
            PlayerColour playerColour = switch (playerID) {
                case 1 -> PlayerColour.RED;
                case 2 -> PlayerColour.BLUE;
                case 3 -> PlayerColour.GREEN;
                default -> PlayerColour.ORANGE;
            };
            player = new Player(playerColour, playerID);
            gameState = GameState.LOBBY;

            csc.sendPlayer();
            csc.listenForServerUpdates();
        }

        public void startNormalGame() {
            csc.startNormalGame();
        }

        public void addSettlement() {
            csc.addSettlement();
        }

        public void upgradeToCity() {
            csc.upgradeToCity();
        }

        public void addRoad() {
            csc.addRoad();
        }

        public void updateTurn() {
            csc.updateTurn();
        }

        public void updateResources() {
            csc.updateResources();
        }

        public void endGame() {
            csc.endGame();
        }

        public void resetGame() {
            csc.resetGame();
        }

        public class ClientSideConnection {
            private Socket socket;
            private ObjectInputStream dataIn;
            private ObjectOutputStream dataOut;
            private int playerID;

            public ClientSideConnection() {
                System.out.println("---Client---");
                try {
                    socket = new Socket("25.47.99.90", 44444);
                    dataOut = new ObjectOutputStream(socket.getOutputStream());
                    dataOut.flush();
                    dataIn = new ObjectInputStream(socket.getInputStream());
                    playerID = dataIn.readInt();
                    tilesDict = (HashMap<ArrayList<Integer>, Tile>) dataIn.readObject();
                    nodesDict = (HashMap<ArrayList<Integer>, Node>) dataIn.readObject();
                    townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                    roadsDict = (HashMap<ArrayList<ArrayList<Integer>>, Road>) dataIn.readObject();
                    currentPlayerTurn = dataIn.readInt();
                } catch (IOException e) {
                    System.out.println("IO Exception occurred from CSC Constructor: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            public void sendPlayer() {
                try {
                    dataOut.writeObject(NEW_PLAYER);
                    dataOut.writeObject(player);
                    dataOut.flush();
                    System.out.println("sendPlayer CSC");
                } catch (IOException e) {
                    System.out.println("IOException from sendPlayer() CSC");
                }
            }

            public void startNormalGame() {
                try {
                    dataOut.writeObject(START_NORMAL_GAME);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException from startNormalGame() CSC");
                }
            }

            public void addSettlement() {
                try {
                    dataOut.writeObject(NEW_TOWN);
                    dataOut.writeObject(nodesDict);
                    dataOut.writeObject(townsDict);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                    System.out.println("addSettlement CSC");
                } catch (IOException e) {
                    System.out.println("IOException occurred from addSettlement() CSC");
                }
            }

            public void upgradeToCity() {
                try {
                    dataOut.writeObject(NEW_CITY);
                    dataOut.writeObject(townsDict);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from upgradeToCity() CSC");
                }
            }

            public void addRoad() {
                try {
                    dataOut.writeObject(NEW_ROAD);
                    dataOut.writeObject(roadsDict);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                    System.out.println("addRoad CSC");
                } catch (IOException e) {
                    System.out.println("IOException occurred from addRoad() CSC");
                }

            }

            public void updateTurn() {
                try {
                    dataOut.writeObject(NEW_TURN);
                    dataOut.writeInt(currentPlayerTurn);
                    dataOut.flush();
                    System.out.println("updateTurn CSC");
                } catch (IOException e) {
                    System.out.println("IOException occurred from updateTurn() CSC");
                }
            }

            public void updateResources() {
                try {
                    dataOut.writeObject(NEW_RESOURCES);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from updateResources() CSC");
                }
            }

            public void endGame() {
                try {
                    dataOut.writeObject(END_GAME);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from endGame() CSC");
                }
            }

            public void resetGame() {
                try {
                    dataOut.writeObject(NEW_GAME);
                } catch (IOException e) {
                    System.out.println("IOException occurred from resetGame() CSC");
                }
            }

            public void listenForServerUpdates() {
                new Thread(() -> {
                    try {
                        while (true) {
                            Object msg = dataIn.readObject();
                            if (msg instanceof SCMessages command) {
                                switch (command) {
                                    case INITIAL_PLACEMENT, NORMAL_GAME_STARTED:
                                        gameState = (GameState) dataIn.readObject();
                                        //allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        System.out.println(gameState);
                                        break;
                                    case NEW_TOWN_ADDED:
                                        nodesDict = (HashMap<ArrayList<Integer>, Node>) dataIn.readObject();
                                        townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        displayUserStats();
                                        break;
                                    case NEW_ROAD_ADDED:
                                        roadsDict = (HashMap<ArrayList<ArrayList<Integer>>, Road>) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        displayUserStats();
                                        break;
                                    case NEW_TURN_ADDED:
                                        currentPlayerTurn = dataIn.readInt();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        if (currentPlayerTurn == player.getPlayerNumber()) {
                                            if (gameState == GameState.INITIAL_PLACEMENT) {
                                                buildSettlementButton.setVisible(true);
                                            }
                                            if (gameState == GameState.NORMAL_PLAY) {
                                                rollDiceButton.setVisible(true);
                                                buildRoadButton.setVisible(false);
                                                buildSettlementButton.setVisible(false);
                                                upgradeSettlementButton.setVisible(false);
                                                endTurnButton.setVisible(false);
                                            }
                                        }
                                        displayUserStats();
                                        break;
                                    case NEW_RESOURCES_ADDED:
                                        if (currentPlayerTurn == player.getPlayerNumber()) {
                                            if (gameState == GameState.NORMAL_PLAY) {
                                                rollDiceButton.setVisible(false);
                                                buildRoadButton.setVisible(true);
                                                buildSettlementButton.setVisible(true);
                                                upgradeSettlementButton.setVisible(true);
                                                endTurnButton.setVisible(true);
                                            }
                                        }
                                        break;
                                    case NEW_CITY_ADDED:
                                        townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        displayUserStats();
                                        break;
                                    case END_GAME_ADDED:
                                        gameState = (GameState) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        endOfGame();
                                }
                                repaint();
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Exception in listenForServerUpdates() CSC " + e.getMessage());
                    }
                }).start();
            }

            public void displayUserStats() {
                int counter = 0;
                for (Player currentPlayer : allPlayers) {
                    if (currentPlayerTurn == currentPlayer.getPlayerNumber()) {
                        currentPlayerLabel.setText("Current Player: " + currentPlayer.getPlayerColour());
                    }
                    if (player.getPlayerNumber() == currentPlayer.getPlayerNumber()) {
                        thisPlayerScoreLabel.setText("Your Score: " + currentPlayer.getScore());
                        thisPlayerLabel.setText("Your Colour: " + currentPlayer.getPlayerColour());
                    }
                    else {
                        switch (counter) {
                            case 0:
                                player1Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                counter++;
                                break;
                            case 1:
                                player2Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                counter++;
                                break;
                            case 2:
                                player3Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                counter++;
                                break;
                        }
                    }
                }
            }
        }
    }
}
