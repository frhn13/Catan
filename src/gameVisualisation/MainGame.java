package gameVisualisation;

import Constants.GameState;
import Constants.ResourceType;
import gameObjects.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static Constants.Constants.*;


public class MainGame extends JFrame implements ActionListener, MouseListener {
    JPanel gamePanel;
    JButton rollDiceButton;
    JButton buildRoadButton;
    JButton buildSettlementButton;
    JButton upgradeSettlementButton;
    JButton endTurnButton;
    JLabel diceRollLabel;
    JLabel currentUserScoreLabel;

    int diceValue;
    boolean buildingNewSettlement = false;
    boolean upgradingToCity = false;
    boolean buildingNewRoad = false;

    ArrayList<Player> players = GameBoard.getAllPlayers();
    HashMap<ArrayList<Integer>, Tile> tilesDict = GameBoard.getTilesDict();
    HashMap<ArrayList<Integer>, Node> nodesDict = GameBoard.getNodesDict();
    HashMap<ArrayList<Integer>, Town> townsDict = GameBoard.getTownsDict();
    HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict = GameBoard.getRoadsDict();

    GameState gameState = GameState.INITIAL_PLACEMENT;

    public MainGame() {
        gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Player currentPlayer = findCurrentPlayer();
                System.out.println(currentPlayer.getPlayerNumber());
                int card_num = 0;
                for (ResourceType resource : currentPlayer.getPlayerResourcesDict().keySet()) {
                    if (currentPlayer.getPlayerResourcesDict().get(resource) > 0) {
                        Image cardImg = new ImageIcon(getClass().getResource("/Images/gameCards/" + resource.cardImage)).getImage();
                        g.drawImage(cardImg, card_num * 100 + 50, DEFAULT_GAME_HEIGHT - 130, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(new Font("Arial", Font.BOLD, 30));
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(currentPlayer.getPlayerResourcesDict().get(resource)), card_num * 100 + 70, DEFAULT_GAME_HEIGHT - 90);
                        card_num++;
//                        for (int i=0; i<currentPlayer.getPlayerResourcesDict().get(resource); i++) {
//                            Image cardImg = new ImageIcon(getClass().getResource("/Images/gameCards/" + resource.cardImage)).getImage();
//                            g.drawImage(cardImg, card_num * 100 + 50, DEFAULT_GAME_HEIGHT - 130, CARD_WIDTH, CARD_HEIGHT, null);
//                            card_num++;
//                        }
                    }
                }

                HashMap<ArrayList<Integer>, Tile> tilesDict = GameBoard.getTilesDict();
                int base_x;
                for (ArrayList<Integer> tile : tilesDict.keySet()) {
                    //g.setColor(tilesDict.get(tile).getTileResource().colour);
                    base_x = switch (tilesDict.get(tile).getTileCoordinates().getLast()) {
                        case 0, 4 -> 300;
                        case 1, 3 -> 200;
                        default -> 100;
                    };
                    try {
                        Image tileImg = new ImageIcon(getClass().getResource("/Images/gameTiles/" + tilesDict.get(tile).getTileResource().tileImage)).getImage();
                        g.drawImage(tileImg, tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 150 + 50, TILE_WIDTH, TILE_HEIGHT, null);
                        //g.fillRect(tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 200 + 50, 100, 100);
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
//                    System.out.println(nodesDict.get(node).getNodeCoordinates().getFirst() + "," +
//                            nodesDict.get(node).getNodeCoordinates().getLast() + "; " +
//                            nodesDict.get(node).getNodeBoardCoordinates().getFirst() + ", " +
//                            nodesDict.get(node).getNodeBoardCoordinates().getLast());
                }

                for (ArrayList<Integer> town : townsDict.keySet()) {
                    g.setColor(currentPlayer.getPlayerColour().colour);
                    if (townsDict.get(town).isCity())
                        g.fillRect(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 20, 20);
                    else
                        g.fillOval(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 20, 20);
                }

                for (ArrayList<ArrayList<Integer>> road : roadsDict.keySet()) {
                    g.setColor(currentPlayer.getPlayerColour().colour);
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
                                    Player currentPlayer = findCurrentPlayer();
                                    Town newTown = new Town(nodesDict.get(node).getNodeCoordinates(), nodesDict.get(node).getConnectedNodes(),
                                            nodesDict.get(node).getConnectedTiles(), currentPlayer.getPlayerColour(), nodesDict.get(node).getNodeBoardCoordinates());
                                    currentPlayer.updatePlayerTownsDict(newTown);
                                    GameBoard.updatePlayerTownsDict(newTown);
                                    currentPlayer.setScore(currentPlayer.getScore() + 1);

                                    currentPlayer.setInitialPlacements(currentPlayer.getInitialPlacements() + 1);
                                    buildingNewSettlement = false;
                                    nodesDict.get(node).setHasSettlement(true);

                                    buildSettlementButton.setVisible(false);
                                    buildRoadButton.setVisible(true);

                                    if (currentPlayer.getInitialPlacements() == TOTAL_INITIAL_PLACEMENTS) {
                                        ArrayList<Tile> tiles = nodesDict.get(node).getConnectedTiles();
                                        HashMap<ResourceType, Integer> newResources = new HashMap<>();
                                        for (Tile tile : tiles)
                                            newResources.put(tile.getTileResource(), 1);
                                        currentPlayer.updatePlayerResourcesDict(newResources);
                                    }
                                    break;
                                } else if (gameState == GameState.NORMAL_PLAY) {
                                    Player currentPlayer = findCurrentPlayer();
                                    outerLoop:
                                    for (ArrayList<ArrayList<Integer>> roads : currentPlayer.getPlayerRoadsDict().keySet()) {
                                        for (ArrayList<Integer> roadNode : roads) {
                                            if (roadNode.equals(node)) {
                                                Town newTown = new Town(nodesDict.get(node).getNodeCoordinates(), nodesDict.get(node).getConnectedNodes(),
                                                        nodesDict.get(node).getConnectedTiles(), currentPlayer.getPlayerColour(), nodesDict.get(node).getNodeBoardCoordinates());
                                                currentPlayer.updatePlayerTownsDict(newTown);
                                                GameBoard.updatePlayerTownsDict(newTown);
                                                currentPlayer.setScore(currentPlayer.getScore() + 1);

                                                HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                                removedResources.put(ResourceType.LUMBER, -1);
                                                removedResources.put(ResourceType.BRICK, -1);
                                                removedResources.put(ResourceType.GRAIN, -1);
                                                removedResources.put(ResourceType.WOOL, -1);

                                                currentPlayer.updatePlayerResourcesDict(removedResources);
                                                currentPlayer.setInitialPlacements(currentPlayer.getInitialPlacements() + 1);
                                                buildingNewSettlement = false;
                                                nodesDict.get(node).setHasSettlement(true);
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
                    HashMap<ArrayList<Integer>, Town> playerTownsDict = currentPlayer.getPlayerTownsDict();
                    for (ArrayList<Integer> town : playerTownsDict.keySet()) {
                        if (mouseX >= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() &&
                                mouseX <= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() + 20 &&
                                mouseY >= playerTownsDict.get(town).getTownBoardCoordinates().getLast() &&
                                mouseY <= playerTownsDict.get(town).getTownBoardCoordinates().getLast() + 20 &&
                                !playerTownsDict.get(town).isCity()) {
                            System.out.println(playerTownsDict.get(town).getTownCoordinates());
                            playerTownsDict.get(town).setCity(true);
                            currentPlayer.updatePlayerTownsDict(playerTownsDict.get(town));
                            currentPlayer.setScore(currentPlayer.getScore() + 1);

                            HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                            removedResources.put(ResourceType.GRAIN, -2);
                            removedResources.put(ResourceType.ORE, -3);

                            currentPlayer.updatePlayerResourcesDict(removedResources);
                            upgradingToCity = false;
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
                        for (ArrayList<ArrayList<Integer>> road : currentPlayer.getPlayerRoadsDict().keySet()) {
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
                                                currentPlayer.getPlayerColour(),
                                                new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(startNodeCoordinates.getFirst(), startNodeCoordinates.getLast())),
                                                        new ArrayList<>(Arrays.asList(endNodeCoordinates.getFirst(), endNodeCoordinates.getLast())))));
                                        currentPlayer.updatePlayerRoadsDict(newRoad);
                                        GameBoard.updateRoadsDict(newRoad);

                                        HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                        removedResources.put(ResourceType.LUMBER, -1);
                                        removedResources.put(ResourceType.BRICK, -1);

                                        currentPlayer.updatePlayerResourcesDict(removedResources);
                                        buildingNewRoad = false;
                                        break outerLoop;
                                    }
                                }
                            }
                        }
                    }

                    outerLoop:
                    for (ArrayList<Integer> town : currentPlayer.getPlayerTownsDict().keySet()) {
                        for (Node neighbourNode : currentPlayer.getPlayerTownsDict().get(town).getConnectedNodes()) {
                            ArrayList<Integer> townCoordinates = currentPlayer.getPlayerTownsDict().get(town).getTownBoardCoordinates();
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
                                        currentPlayer.getPlayerColour(),
                                        new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(townCoordinates.getFirst(), townCoordinates.getLast())),
                                            new ArrayList<>(Arrays.asList(nodeCoordinates.getFirst(), nodeCoordinates.getLast())))));
                                currentPlayer.updatePlayerRoadsDict(newRoad);
                                GameBoard.updateRoadsDict(newRoad);

                                if (gameState == GameState.NORMAL_PLAY) {
                                    HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                                    removedResources.put(ResourceType.LUMBER, -1);
                                    removedResources.put(ResourceType.BRICK, -1);
                                    currentPlayer.updatePlayerResourcesDict(removedResources);
                                }

                                if (gameState == GameState.INITIAL_PLACEMENT) {
                                    buildRoadButton.setVisible(false);
                                    buildSettlementButton.setVisible(true);
                                    if (currentPlayer.getInitialPlacements() >= 2) {
                                        gameState = GameState.NORMAL_PLAY;
                                        rollDiceButton.setVisible(true);
                                        buildRoadButton.setVisible(false);
                                        buildSettlementButton.setVisible(false);
                                        upgradeSettlementButton.setVisible(false);
                                        endTurnButton.setVisible(false);
                                    }
                                }

                                buildingNewRoad = false;
                                break outerLoop;
                            }
                        }
                    }
                }
                gamePanel.repaint();
                Player currentPlayer = findCurrentPlayer();
                currentUserScoreLabel.setText("Your Score: " + String.valueOf(currentPlayer.getScore()));

                System.out.println("BRICK: " + currentPlayer.getPlayerResourcesDict().get(ResourceType.BRICK));
                System.out.println("LUMBER: " + currentPlayer.getPlayerResourcesDict().get(ResourceType.LUMBER));
                System.out.println("GRAIN: " + currentPlayer.getPlayerResourcesDict().get(ResourceType.GRAIN));
                System.out.println("WOOL: " + currentPlayer.getPlayerResourcesDict().get(ResourceType.WOOL));
                System.out.println("ORE: " + currentPlayer.getPlayerResourcesDict().get(ResourceType.ORE));
                System.out.println(gameState);
            }
        });

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

        currentUserScoreLabel = new JLabel("Your Score: 0") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 300, 300, 100);
            }
        };

        if (gameState == GameState.INITIAL_PLACEMENT) {
            buildRoadButton.setVisible(false);
            rollDiceButton.setVisible(false);
            upgradeSettlementButton.setVisible(false);
            endTurnButton.setVisible(false);
            diceRollLabel.setVisible(false);
            diceRollLabel.setFont(DICE_ROLL_FONT);
            currentUserScoreLabel.setFont(SCORE_FONT);
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
                HashMap<ArrayList<Integer>, Town> playerTownsDict = currentPlayer.getPlayerTownsDict();

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

                rollDiceButton.setVisible(false);
                buildRoadButton.setVisible(true);
                buildSettlementButton.setVisible(true);
                upgradeSettlementButton.setVisible(true);
                endTurnButton.setVisible(true);

                currentPlayer.updatePlayerResourcesDict(newResources);
            }
        });

        buildRoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                upgradingToCity = false;
                Player currentPlayer = findCurrentPlayer();
                if ((currentPlayer.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                currentPlayer.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
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
                if ((currentPlayer.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.WOOL) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
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
                if (currentPlayer.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 2 &&
                currentPlayer.getPlayerResourcesDict().get(ResourceType.ORE) >= 3) {
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

                rollDiceButton.setVisible(true);
                buildRoadButton.setVisible(false);
                buildSettlementButton.setVisible(false);
                upgradeSettlementButton.setVisible(false);
                endTurnButton.setVisible(false);
            }
        });

        gamePanel.add(rollDiceButton);
        gamePanel.add(buildRoadButton);
        gamePanel.add(buildSettlementButton);
        gamePanel.add(upgradeSettlementButton);
        gamePanel.add(endTurnButton);
        gamePanel.add(diceRollLabel);
        gamePanel.add(currentUserScoreLabel);

        this.setSize(DEFAULT_GAME_WIDTH, DEFAULT_GAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        //this.pack();
        this.add(gamePanel);
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
        for (Player player : players) {
            if (player.getPlayerNumber() == GameBoard.getCurrentPlayerTurn())
                return player;
        }
        return players.getFirst();
    }
}
