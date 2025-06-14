package gameVisualisation;

import Constants.ResourceType;
import gameObjects.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static Constants.Constants.*;


public class MainGame extends JFrame implements ActionListener, MouseListener {
    JPanel gamePanel;
    JButton rollDiceButton;
    JButton buildRoadButton;
    JButton buildSettlementButton;
    JButton upgradeSettlementButton;
    JLabel diceRollLabel;

    int diceValue;
    boolean buildingNewSettlement = false;
    boolean upgradingToCity = false;
    boolean buildingNewRoad = false;

    ArrayList<Player> players = GameBoard.getAllPlayers();
    HashMap<ArrayList<Integer>, Tile> tilesDict = GameBoard.getTilesDict();
    HashMap<ArrayList<Integer>, Node> nodesDict = GameBoard.getNodesDict();
    HashMap<ArrayList<Integer>, Town> townsDict = GameBoard.getTownsDict();

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
                        for (int i=0; i<currentPlayer.getPlayerResourcesDict().get(resource); i++) {
                            Image cardImg = new ImageIcon(getClass().getResource("/Images/gameCards/" + resource.cardImage)).getImage();
                            g.drawImage(cardImg, card_num * 100 + 50, DEFAULT_GAME_HEIGHT - 130, CARD_WIDTH, CARD_HEIGHT, null);
                            card_num++;
                        }
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
                    base_x = switch (townsDict.get(town).getTownCoordinates().getLast()) {
                        case 0, 11 -> 400;
                        case 1, 2, 9, 10 -> 300;
                        case 3, 4, 7, 8 -> 200;
                        default -> 100;
                    };
                    int y_pos = switch (townsDict.get(town).getTownCoordinates().getLast()) {
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
                    g.setColor(currentPlayer.getPlayerColour().colour);
                    if (townsDict.get(town).isCity())
                        g.fillRect(townsDict.get(town).getTownCoordinates().getFirst() * 200 + base_x - 10, y_pos - 10, 20, 20);
                    else
                        g.fillOval(townsDict.get(town).getTownCoordinates().getFirst() * 200 + base_x - 10, y_pos - 10, 20, 20);
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
                                System.out.println(nodesDict.get(node).getNodeCoordinates());

                                Player currentPlayer = findCurrentPlayer();
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
                                buildingNewSettlement = false;
                                nodesDict.get(node).setHasSettlement(true);
                            }
                        }
                    }
                }

                if (upgradingToCity) {
                    for (ArrayList<Integer> town : townsDict.keySet()) {
                        if (mouseX >= townsDict.get(town).getTownBoardCoordinates().getFirst() &&
                                mouseX <= townsDict.get(town).getTownBoardCoordinates().getFirst() + 20 &&
                                mouseY >= townsDict.get(town).getTownBoardCoordinates().getLast() &&
                                mouseY <= townsDict.get(town).getTownBoardCoordinates().getLast() + 20 &&
                                !townsDict.get(town).isCity()) {
                            Player currentPlayer = findCurrentPlayer();
                            System.out.println(townsDict.get(town).getTownCoordinates());
                            townsDict.get(town).setCity(true);
                            currentPlayer.updatePlayerTownsDict(townsDict.get(town));
                            currentPlayer.setScore(currentPlayer.getScore() + 1);

                            HashMap<ResourceType, Integer> removedResources = new HashMap<>();
                            removedResources.put(ResourceType.GRAIN, -2);
                            removedResources.put(ResourceType.ORE, -3);

                            currentPlayer.updatePlayerResourcesDict(removedResources);
                            upgradingToCity = false;
                        }
                    }
                }
                gamePanel.repaint();
            }
        });

        rollDiceButton = new JButton("Roll Dice") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(100, DEFAULT_GAME_HEIGHT - 200, 200, 50);
            }
        };

        buildRoadButton = new JButton("Build Road") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(350, DEFAULT_GAME_HEIGHT - 200, 200, 50);
            }
        };

        buildSettlementButton = new JButton("Build Settlement") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(600, DEFAULT_GAME_HEIGHT - 200, 200, 50);
            }
        };

        upgradeSettlementButton = new JButton("Upgrade to City") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 200, 200, 50);
            }
        };

        diceRollLabel = new JLabel() {
          public void setBounds(int x, int y, int width, int height) {
              super.setBounds(DEFAULT_GAME_WIDTH / 2, DEFAULT_GAME_HEIGHT - 300, 100, 100);
          }
        };
        diceRollLabel.setVisible(false);
        diceRollLabel.setFont(DICE_ROLL_FONT);

        rollDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random random = new Random();
                int diceRoll1 = random.nextInt(6) + 1;
                int diceRoll2 = random.nextInt(6) + 1;
                int diceValue = diceRoll1 + diceRoll2;
                diceRollLabel.setVisible(true);
                diceRollLabel.setText(String.valueOf(diceValue));

                Player currentPlayer = findCurrentPlayer();

                HashMap<ResourceType, Integer> newResources = new HashMap<>();
                for (Tile tile : tilesDict.values()) {
                    if (tile.getRollValue() == diceValue) {
                        if (newResources.containsKey(tile.getTileResource()))
                            newResources.put(tile.getTileResource(), newResources.get(tile.getTileResource()) + 1);
                        else
                            newResources.put(tile.getTileResource(), 1);
                    }
                }
                currentPlayer.updatePlayerResourcesDict(newResources);
                gamePanel.repaint();
            }
        });

        buildRoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                upgradingToCity = false;
                Player currentPlayer = findCurrentPlayer();
                if (currentPlayer.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                currentPlayer.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1) {
                    buildingNewRoad = !buildingNewRoad;
                }
                System.out.println("B");
            }
        });

        buildSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upgradingToCity = false;
                buildingNewRoad = false;
                Player currentPlayer = findCurrentPlayer();
                if (currentPlayer.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 1 &&
                        currentPlayer.getPlayerResourcesDict().get(ResourceType.WOOL) >= 1) {
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

        gamePanel.add(rollDiceButton);
        gamePanel.add(buildRoadButton);
        gamePanel.add(buildSettlementButton);
        gamePanel.add(upgradeSettlementButton);
        gamePanel.add(diceRollLabel);

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
