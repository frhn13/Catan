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
    String ip_address;
    JPanel gamePanel;
    JButton rollDiceButton;
    JButton buildRoadButton;
    JButton buildSettlementButton;
    JButton upgradeSettlementButton;
    JButton tradeButton;
    JButton endTurnButton;
    JLabel diceRollLabel;

    JLabel thisPlayerScoreLabel = new JLabel();
    JLabel thisPlayerLabel;
    JLabel thisPlayerCardsLabel = new JLabel();
    JLabel player1Label = new JLabel();
    JLabel player2Label = new JLabel();
    JLabel player3Label = new JLabel();
    JLabel player1cardsLabel = new JLabel();
    JLabel player2cardsLabel = new JLabel();
    JLabel player3cardsLabel = new JLabel();
    JLabel currentPlayerLabel = new JLabel();
    JLabel moveRobberLabel = new JLabel();

    JButton bankTradeButton;
    JButton playerTradeButton;
    JButton acceptTradeButton;
    JButton rejectTradeButton;
    JButton discardCardsButton;

    JPanel endgamePanel;
    JLabel scoresLabel;
    JLabel numberOfSettlementsLabel;
    JLabel numberOfCitiesLabel;
    JButton newGameButton;
    JLabel waitingLabel;

    int diceValue;
    int currentPlayerTurn;
    HashMap<ResourceType, Integer> newResources;
    HashMap<PlayerColour, ArrayList<Integer>> playersToRob;

    boolean buildingNewSettlement = false;
    boolean upgradingToCity = false;
    boolean buildingNewRoad = false;
    boolean tradingResources = false;
    boolean tradeOffer = false;
    boolean moveRobber = false;
    boolean robPlayer = false;
    boolean discardCards = false;

    StringBuilder finalScores = new StringBuilder();
    StringBuilder finalSettlements = new StringBuilder();
    StringBuilder finalCities = new StringBuilder();

    Player player;
    Player tradingPlayer;
    Player robbedPlayer;
    GameClient gameClient = new GameClient();

    ArrayList<Player> allPlayers;
    ArrayList<Player> discardingPlayers;
    HashMap<ArrayList<Integer>, Tile> tilesDict;
    HashMap<ArrayList<Integer>, Node> nodesDict;
    HashMap<ArrayList<Integer>, Town> townsDict;
    HashMap<ArrayList<ArrayList<Integer>>, Road> roadsDict;
    HashMap<ResourceType, Integer> currentPlayerGivingTrade = new HashMap<>();
    HashMap<ResourceType, Integer> currentPlayerTakingTrade = new HashMap<>();
    HashMap<ResourceType, Integer> newPlayerGivingTrade = new HashMap<>();
    HashMap<ResourceType, Integer> newPlayerTakingTrade = new HashMap<>();
    HashMap<ArrayList<Integer>, ResourceType> givingTradeCoordinates = new HashMap<>();
    HashMap<ArrayList<Integer>, ResourceType> takingTradeCoordinates = new HashMap<>();
    HashMap<ResourceType, Integer> discardedCards = new HashMap<>();
    HashMap<ArrayList<Integer>, ResourceType> playerDiscardResourcesCoordinates = new HashMap<>();

    GameState gameState = GameState.INITIAL_PLACEMENT;

    public MainGame(String inserted_ip) {
        ip_address = inserted_ip;
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

        waitingLabel = new JLabel("Waiting for other players...") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH / 2 - 100, DEFAULT_GAME_HEIGHT - 200, 1000, 400);
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
                resetTrading();

                buildingNewSettlement = false;
                upgradingToCity = false;
                buildingNewRoad = false;

                gameState = GameState.INITIAL_PLACEMENT;
                newGameButton.setVisible(false);
                waitingLabel.setVisible(true);
            }
        });

        endgamePanel.add(scoresLabel);
        endgamePanel.add(numberOfSettlementsLabel);
        endgamePanel.add(numberOfCitiesLabel);
        endgamePanel.add(newGameButton);
        endgamePanel.add(waitingLabel);
        waitingLabel.setVisible(false);

        // Code for panel during the game
        gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                int card_num = 0;
                if (tradingResources) {
                    for (ResourceType resource : ResourceType.values()) {
                        if (resource != ResourceType.DESERT) {
                            Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                            g.drawImage(cardImg, card_num * 60 + 50, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                            takingTradeCoordinates.put(new ArrayList<>(Arrays.asList(card_num * 60 + 50, DEFAULT_GAME_HEIGHT - 600)), resource);
                            g.setFont(RESOURCE_AMOUNT_FONT);
                            g.setColor(Color.black);
                            g.drawString(String.valueOf(currentPlayerTakingTrade.get(resource)), card_num * 60 + 70, DEFAULT_GAME_HEIGHT - 560);
                        }
                        card_num++;
                    }
                    card_num = 0;
                    for (ResourceType resource : ResourceType.values()) {
                        if (resource != ResourceType.DESERT) {
                            Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                            g.drawImage(cardImg, card_num * 60 + 50, DEFAULT_GAME_HEIGHT - 500, CARD_WIDTH, CARD_HEIGHT, null);
                            givingTradeCoordinates.put(new ArrayList<>(Arrays.asList(card_num * 60 + 50, DEFAULT_GAME_HEIGHT - 500)), resource);
                            g.setFont(RESOURCE_AMOUNT_FONT);
                            g.setColor(Color.black);
                            g.drawString(String.valueOf(currentPlayerGivingTrade.get(resource)), card_num * 60 + 70, DEFAULT_GAME_HEIGHT - 460);
                        }
                        card_num++;
                    }
                    Image greenArrowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/trade/green_arrow.png"))).getImage();
                    Image redArrowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/trade/red_arrow.png"))).getImage();
                    g.drawImage(greenArrowImg, card_num * 60 + 10, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                    g.drawImage(redArrowImg, card_num * 60 + 10, DEFAULT_GAME_HEIGHT - 500, CARD_WIDTH, CARD_HEIGHT, null);
                }

                if (tradeOffer && tradingPlayer.getPlayerNumber() != player.getPlayerNumber()) {
                    acceptTradeButton.setVisible(true);
                    rejectTradeButton.setVisible(true);
                    card_num = 0;
                    for (ResourceType resource : newPlayerTakingTrade.keySet()) {
                        Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                        g.drawImage(cardImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(RESOURCE_AMOUNT_FONT);
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(newPlayerTakingTrade.get(resource)), DEFAULT_GAME_WIDTH - card_num * 60 - 80, DEFAULT_GAME_HEIGHT - 560);
                        card_num++;
                    }
                    card_num = 0;
                    for (ResourceType resource : newPlayerGivingTrade.keySet()) {
                        Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                        g.drawImage(cardImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 500, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(RESOURCE_AMOUNT_FONT);
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(newPlayerGivingTrade.get(resource)), DEFAULT_GAME_WIDTH - card_num * 60 - 80, DEFAULT_GAME_HEIGHT - 460);
                        card_num++;
                    }
                    Image greenArrowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/trade/green_arrow.png"))).getImage();
                    Image redArrowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/trade/red_arrow.png"))).getImage();
                    g.drawImage(greenArrowImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                    g.drawImage(redArrowImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 500, CARD_WIDTH, CARD_HEIGHT, null);
                }

                card_num = 0;
                for (ResourceType resource : player.getPlayerResourcesDict().keySet()) {
                    if (player.getPlayerResourcesDict().get(resource) > 0) {
                        Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resource.cardImage))).getImage();
                        g.drawImage(cardImg, card_num * 100 + 50, DEFAULT_GAME_HEIGHT - 130, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(RESOURCE_AMOUNT_FONT);
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(player.getPlayerResourcesDict().get(resource)), card_num * 100 + 70, DEFAULT_GAME_HEIGHT - 90);
                        card_num++;
                    }
                }

                int base_x;
                for (ArrayList<Integer> tile : tilesDict.keySet()) {
                    base_x = switch (tilesDict.get(tile).getTileCoordinates().getLast()) {
                        case 0, 4 -> 450;
                        case 1, 3 -> 400;
                        default -> 350;
                    };
                    try {
                        tilesDict.get(tile).setTileBoardCoordinates(new ArrayList<>(Arrays.asList(tilesDict.get(tile).getTileCoordinates().getFirst() * TILE_WIDTH + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 75 + 50)));
                        Image tileImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameTiles/" + tilesDict.get(tile).getTileResource().tileImage))).getImage();
                        g.drawImage(tileImg, tilesDict.get(tile).getTileCoordinates().getFirst() * TILE_WIDTH + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 75 + 50, TILE_WIDTH, TILE_HEIGHT, null);
                        if (tilesDict.get(tile).getRollValue() != 0) {
                            g.setFont(RESOURCE_AMOUNT_FONT);
                            g.setColor(Color.white);
                            g.drawString(String.valueOf(tilesDict.get(tile).getRollValue()), tilesDict.get(tile).getTileCoordinates().getFirst() * TILE_WIDTH + base_x + 40, tilesDict.get(tile).getTileCoordinates().getLast() * 75 + 110);
                        }
                        if (tilesDict.get(tile).isTileBlocked()) {
                            g.setColor(Color.magenta);
                            g.fillOval(tilesDict.get(tile).getTileCoordinates().getFirst() * TILE_WIDTH + (base_x - 10) + (TILE_WIDTH / 2), tilesDict.get(tile).getTileCoordinates().getLast() * 75 + 120, 10, 10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (ArrayList<Integer> node : nodesDict.keySet()) {
                    base_x = switch (nodesDict.get(node).getNodeCoordinates().getLast()) {
                        case 0, 11 -> 500;
                        case 1, 2, 9, 10 -> 450;
                        case 3, 4, 7, 8 -> 400;
                        default -> 350;
                    };
                    int y_pos = switch (nodesDict.get(node).getNodeCoordinates().getLast()) {
                        case 0 -> 50;
                        case 1 -> 75;
                        case 2 -> 125;
                        case 3 -> 150;
                        case 4 -> 200;
                        case 5 -> 225;
                        case 6 -> 275;
                        case 7 -> 300;
                        case 8 -> 350;
                        case 9 -> 375;
                        case 10 -> 425;
                        default -> 450;
                    };
                    g.setColor(Color.BLACK);
                    g.fillOval(nodesDict.get(node).getNodeCoordinates().getFirst() * TILE_WIDTH + base_x - 5, y_pos - 5, 10, 10);
                    nodesDict.get(node).setNodeBoardCoordinates(new ArrayList<>(Arrays.asList(nodesDict.get(node).getNodeCoordinates().getFirst() * TILE_WIDTH + base_x - 5, y_pos - 5)));
                }

                for (ArrayList<Integer> town : townsDict.keySet()) {
                    g.setColor(townsDict.get(town).getTownColour().colour);
                    if (townsDict.get(town).isCity())
                        g.fillRect(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 10, 10);
                    else
                        g.fillOval(townsDict.get(town).getTownBoardCoordinates().getFirst(), townsDict.get(town).getTownBoardCoordinates().getLast(), 10, 10);
                }

                for (ArrayList<ArrayList<Integer>> road : roadsDict.keySet()) {
                    g.setColor(roadsDict.get(road).getRoadColour().colour);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(roadsDict.get(road).getRoadNodeBoardCoordinates().getFirst().getFirst() + 5,
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getFirst().getLast(),
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getLast().getFirst() + 5,
                            roadsDict.get(road).getRoadNodeBoardCoordinates().getLast().getLast());
                }

                if (robPlayer) {
                    card_num = 0;
                    for (PlayerColour playerToRob : playersToRob.keySet()) {
                        g.setColor(playerToRob.colour);
                        g.fillRect(DEFAULT_GAME_WIDTH / 2 + card_num * 30, DEFAULT_GAME_HEIGHT - 500, 20, 20);
                        playersToRob.replace(playerToRob, new ArrayList<>(Arrays.asList(DEFAULT_GAME_WIDTH / 2 + card_num * 30, DEFAULT_GAME_HEIGHT - 500)));
                        card_num++;
                    }
                }

                card_num = 0;
                if (discardCards) {
                    for (ResourceType resourceType : discardedCards.keySet()) {
                        Image cardImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/gameCards/" + resourceType.cardImage))).getImage();
                        g.drawImage(cardImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                        g.setFont(RESOURCE_AMOUNT_FONT);
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(discardedCards.get(resourceType)), DEFAULT_GAME_WIDTH - card_num * 60 - 80, DEFAULT_GAME_HEIGHT - 560);
                        playerDiscardResourcesCoordinates.put(new ArrayList<>(Arrays.asList(DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 600)), resourceType);
                        card_num++;
                    }
                    Image redArrowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/trade/red_arrow.png"))).getImage();
                    g.drawImage(redArrowImg, DEFAULT_GAME_WIDTH - card_num * 60 - 100, DEFAULT_GAME_HEIGHT - 600, CARD_WIDTH, CARD_HEIGHT, null);
                }
            }
        };

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                boolean neighbouring_settlement = false;
                System.out.println(mouseX);
                System.out.println(mouseY);

                if (buildingNewSettlement) {
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
                    HashMap<ArrayList<Integer>, Town> playerTownsDict = player.getPlayerTownsDict();
                    for (ArrayList<Integer> town : playerTownsDict.keySet()) {
                        if (mouseX >= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() &&
                                mouseX <= playerTownsDict.get(town).getTownBoardCoordinates().getFirst() + 20 &&
                                mouseY >= playerTownsDict.get(town).getTownBoardCoordinates().getLast() &&
                                mouseY <= playerTownsDict.get(town).getTownBoardCoordinates().getLast() + 20 &&
                                !playerTownsDict.get(town).isCity()) {
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
                                    if (player.getInitialPlacements() >= 2 && player.getPlayerNumber() == 1)
                                        gameClient.startNormalGame();
                                    if (player.getInitialPlacements() == 1)
                                        currentPlayerTurn = player.getPlayerNumber() < 4 ? player.getPlayerNumber() + 1 : 4;
                                    if (player.getInitialPlacements() == 2)
                                        currentPlayerTurn = player.getPlayerNumber() > 1 ? player.getPlayerNumber() - 1 : 1;
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

                if (tradingResources) {
                    for (ArrayList<Integer> resourceTakingCoordinates : takingTradeCoordinates.keySet()) {
                        if (mouseX >= resourceTakingCoordinates.getFirst() && mouseX <= resourceTakingCoordinates.getFirst() + CARD_WIDTH &&
                        mouseY >= resourceTakingCoordinates.getLast() && mouseY <= resourceTakingCoordinates.getLast() + CARD_HEIGHT) {
                            if (e.getButton() == MouseEvent.BUTTON1)
                                currentPlayerTakingTrade.put(takingTradeCoordinates.get(resourceTakingCoordinates), currentPlayerTakingTrade.get(takingTradeCoordinates.get(resourceTakingCoordinates)) + 1);
                            else if (e.getButton() == MouseEvent.BUTTON3 && currentPlayerTakingTrade.get(takingTradeCoordinates.get(resourceTakingCoordinates)) > 0)
                                currentPlayerTakingTrade.put(takingTradeCoordinates.get(resourceTakingCoordinates), currentPlayerTakingTrade.get(takingTradeCoordinates.get(resourceTakingCoordinates)) - 1);
                        }
                    }
                    for (ArrayList<Integer> resourceGivingCoordinates : givingTradeCoordinates.keySet()) {
                        if (mouseX >= resourceGivingCoordinates.getFirst() && mouseX <= resourceGivingCoordinates.getFirst() + CARD_WIDTH &&
                                mouseY >= resourceGivingCoordinates.getLast() && mouseY <= resourceGivingCoordinates.getLast() + CARD_HEIGHT) {
                            if (e.getButton() == MouseEvent.BUTTON1)
                                currentPlayerGivingTrade.put(givingTradeCoordinates.get(resourceGivingCoordinates), currentPlayerGivingTrade.get(givingTradeCoordinates.get(resourceGivingCoordinates)) + 1);
                            else if (e.getButton() == MouseEvent.BUTTON3 && currentPlayerGivingTrade.get(givingTradeCoordinates.get(resourceGivingCoordinates)) > 0)
                                currentPlayerGivingTrade.put(givingTradeCoordinates.get(resourceGivingCoordinates), currentPlayerGivingTrade.get(givingTradeCoordinates.get(resourceGivingCoordinates)) - 1);
                        }
                    }
                    repaint();
                }

                if (moveRobber) {
                    for (Tile tile : tilesDict.values()) {
                        if (mouseX >= tile.getTileBoardCoordinates().getFirst() && mouseX <= tile.getTileBoardCoordinates().getFirst() + (0.75 * TILE_WIDTH) &&
                        mouseY >= tile.getTileBoardCoordinates().getLast() && mouseY <= tile.getTileBoardCoordinates().getLast() + TILE_WIDTH && !tile.isTileBlocked()) {
                            for (Tile otherTile : tilesDict.values())
                                otherTile.setTileBlocked(false);

                            tile.setTileBlocked(true);
                            gameClient.moveRobber();
                            moveRobber = false;
                            robPlayer(tile);

                            if (!robPlayer) {
                                rollDiceButton.setVisible(false);
                                buildRoadButton.setVisible(true);
                                buildSettlementButton.setVisible(true);
                                upgradeSettlementButton.setVisible(true);
                                tradeButton.setVisible(true);
                                endTurnButton.setVisible(true);
                                moveRobberLabel.setText("");
                            }
                            else
                                moveRobberLabel.setText("Rob Player");
                            break;
                        }
                    }
                }

                if (robPlayer) {
                    outerLoop:
                    for (PlayerColour playerColourToRob : playersToRob.keySet()) {
                        if (mouseX >= playersToRob.get(playerColourToRob).getFirst() && mouseX <= playersToRob.get(playerColourToRob).getFirst() + 20
                                && mouseY >= playersToRob.get(playerColourToRob).getLast() && mouseY <= playersToRob.get(playerColourToRob).getLast() + 20) {
                            for (Player playerToRob : allPlayers) {
                                if (playerToRob.getPlayerColour() == playerColourToRob) {
                                    robbedPlayer = playerToRob;
                                    gameClient.playerRobbed();

                                    robPlayer = false;
                                    rollDiceButton.setVisible(false);
                                    buildRoadButton.setVisible(true);
                                    buildSettlementButton.setVisible(true);
                                    upgradeSettlementButton.setVisible(true);
                                    tradeButton.setVisible(true);
                                    endTurnButton.setVisible(true);
                                    moveRobberLabel.setText("");
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }

                if (discardCards) {
                    for (ArrayList<Integer> coordinates : playerDiscardResourcesCoordinates.keySet()) {
                        if (mouseX >= coordinates.getFirst() && mouseX <= coordinates.getFirst() + CARD_WIDTH &&
                        mouseY >= coordinates.getLast() && mouseY <= coordinates.getLast() + CARD_HEIGHT) {
                            ResourceType resource = playerDiscardResourcesCoordinates.get(coordinates);
                            if (e.getButton() == MouseEvent.BUTTON1)
                                discardedCards.put(resource, discardedCards.get(resource) + 1);
                            else if (e.getButton() == MouseEvent.BUTTON3 && discardedCards.get(resource) > 0)
                                discardedCards.put(resource, discardedCards.get(resource) - 1);
                        }
                    }
                }

                repaint();

                setButtonText();

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

        tradeButton = new JButton("Trade Resources") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 400, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        endTurnButton = new JButton("End Turn") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 200, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        bankTradeButton = new JButton("Bank Trade") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 600, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        playerTradeButton = new JButton("Player Trade") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(850, DEFAULT_GAME_HEIGHT - 500, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        acceptTradeButton = new JButton("✓") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 250, DEFAULT_GAME_HEIGHT - 400, SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
            }
        };

        rejectTradeButton = new JButton("x") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 150, DEFAULT_GAME_HEIGHT - 400, SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
            }
        };

        discardCardsButton = new JButton("Discard Cards") {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 250, DEFAULT_GAME_HEIGHT - 500, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        diceRollLabel = new JLabel() {
          public void setBounds(int x, int y, int width, int height) {
              super.setBounds(DEFAULT_GAME_WIDTH / 2 - 100, DEFAULT_GAME_HEIGHT - 300, 300, 100);
          }
        };

        thisPlayerScoreLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 350, 300, 100);
            }
        };

        thisPlayerLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 400, 400, 100);
            }
        };

        thisPlayerCardsLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, DEFAULT_GAME_HEIGHT - 300, 400, 100);
            }
        };

        currentPlayerLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 25, 400, 100);
            }
        };

        player1Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 75, 400, 100);
            }
        };

        player2Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 150, 400, 100);
            }
        };

        player3Label = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 225, 400, 100);
            }
        };

        player1cardsLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 100, 400, 100);
            }
        };

        player2cardsLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 175, 400, 100);
            }
        };

        player3cardsLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(50, 250, 400, 100);
            }
        };

        moveRobberLabel = new JLabel() {
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(DEFAULT_GAME_WIDTH - 250, DEFAULT_GAME_HEIGHT - 300, GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
            }
        };

        if (gameState == GameState.INITIAL_PLACEMENT || gameState == GameState.LOBBY) {
            resetTrading();
            buildSettlementButton.setVisible(false);
            buildRoadButton.setVisible(false);
            rollDiceButton.setVisible(false);
            upgradeSettlementButton.setVisible(false);
            endTurnButton.setVisible(false);
            diceRollLabel.setVisible(false);
            tradeButton.setVisible(false);
            playerTradeButton.setVisible(false);
            bankTradeButton.setVisible(false);
            acceptTradeButton.setVisible(false);
            rejectTradeButton.setVisible(false);
            discardCardsButton.setVisible(false);
            diceRollLabel.setFont(DICE_ROLL_FONT);
            thisPlayerScoreLabel.setFont(SCORE_FONT);
            thisPlayerLabel.setFont(SCORE_FONT);
            thisPlayerCardsLabel.setFont(SCORE_FONT);
            currentPlayerLabel.setFont(SCORE_FONT);
            player1Label.setFont(OTHER_SCORE_FONT);
            player2Label.setFont(OTHER_SCORE_FONT);
            player3Label.setFont(OTHER_SCORE_FONT);
            player1cardsLabel.setFont(OTHER_SCORE_FONT);
            player2cardsLabel.setFont(OTHER_SCORE_FONT);
            player3cardsLabel.setFont(OTHER_SCORE_FONT);
            moveRobberLabel.setFont(SCORE_FONT);
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
                tradingResources = false;
                resetTrading();
                Random random = new Random();
                int diceRoll1 = random.nextInt(6) + 1;
                int diceRoll2 = random.nextInt(6) + 1;
                diceValue = diceRoll1 + diceRoll2;

                if (diceValue == 7)
                    gameClient.sevenRolled();

                rollDiceButton.setVisible(false);
                gameClient.getResources();
                setButtonText();
                gamePanel.repaint();
            }
        });

        buildRoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                upgradingToCity = false;
                tradingResources = false;
                if ((player.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
                    buildingNewRoad = !buildingNewRoad;
                }
                System.out.println(buildingNewRoad);
                setButtonText();
                gamePanel.repaint();
            }
        });

        buildSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upgradingToCity = false;
                buildingNewRoad = false;
                tradingResources = false;
                if ((player.getPlayerResourcesDict().get(ResourceType.LUMBER) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.BRICK) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 1 &&
                        player.getPlayerResourcesDict().get(ResourceType.WOOL) >= 1) || gameState == GameState.INITIAL_PLACEMENT) {
                    buildingNewSettlement = !buildingNewSettlement;
                }
                System.out.println(buildingNewSettlement);
                setButtonText();
                gamePanel.repaint();
            }
        });

        upgradeSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                buildingNewRoad = false;
                tradingResources = false;
                if (player.getPlayerResourcesDict().get(ResourceType.GRAIN) >= 2 &&
                player.getPlayerResourcesDict().get(ResourceType.ORE) >= 3) {
                    upgradingToCity = !upgradingToCity;
                }
                System.out.println(upgradingToCity);
                setButtonText();
                gamePanel.repaint();
            }
        });

        tradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                buildingNewRoad = false;
                upgradingToCity = false;
                resetTrading();

                tradingResources = !tradingResources;

                System.out.println(tradingResources);
                setButtonText();
                gamePanel.repaint();
            }
        });

        bankTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int totalResources = 0;
                HashMap<ResourceType, Integer> newResources = new HashMap<>();

                for (ResourceType resourceTaken : currentPlayerTakingTrade.keySet()) {
                    totalResources += currentPlayerTakingTrade.get(resourceTaken);
                    newResources.put(resourceTaken, currentPlayerTakingTrade.get(resourceTaken));
                }
                System.out.println(totalResources);
                for (ResourceType resourceGiven : currentPlayerGivingTrade.keySet()) {
                    if (totalResources * 4 <= player.getPlayerResourcesDict().get(resourceGiven) && currentPlayerGivingTrade.get(resourceGiven) == totalResources * 4
                            && (currentPlayerGivingTrade.get(resourceGiven) == 0 || currentPlayerTakingTrade.get(resourceGiven) == 0)) {
                        newResources.put(resourceGiven, totalResources * -4);
                        player.updatePlayerResourcesDict(newResources);
                        gameClient.updateResources();
                        resetTrading();
                        break;
                    }
                }
                gamePanel.repaint();
            }
        });

        playerTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean validTrade = true;
                for (ResourceType resourceGiven : currentPlayerGivingTrade.keySet()) {
                    if (player.getPlayerResourcesDict().get(resourceGiven) < currentPlayerGivingTrade.get(resourceGiven) ||
                            (currentPlayerGivingTrade.get(resourceGiven) != 0 && currentPlayerTakingTrade.get(resourceGiven) != 0))
                        validTrade = false;
                }
                if ((currentPlayerTakingTrade.get(ResourceType.LUMBER) == 0 && currentPlayerTakingTrade.get(ResourceType.BRICK) == 0
                    && currentPlayerTakingTrade.get(ResourceType.WOOL) == 0 && currentPlayerTakingTrade.get(ResourceType.GRAIN) == 0
                    && currentPlayerTakingTrade.get(ResourceType.ORE) == 0) ||
                    (currentPlayerGivingTrade.get(ResourceType.LUMBER) == 0 && currentPlayerGivingTrade.get(ResourceType.BRICK) == 0
                    && currentPlayerGivingTrade.get(ResourceType.WOOL) == 0 && currentPlayerGivingTrade.get(ResourceType.GRAIN) == 0
                    && currentPlayerGivingTrade.get(ResourceType.ORE) == 0))
                    validTrade = false;

                if (validTrade) gameClient.offerPlayerTrade();
            }
        });

        acceptTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean validTrade = true;
                for (ResourceType resourceGiven : newPlayerGivingTrade.keySet()) {
                    if (player.getPlayerResourcesDict().get(resourceGiven) < newPlayerGivingTrade.get(resourceGiven))
                        validTrade = false;
                }
                for (ResourceType resourceTaken : newPlayerTakingTrade.keySet()) {
                    if (tradingPlayer.getPlayerResourcesDict().get(resourceTaken) < newPlayerTakingTrade.get(resourceTaken))
                        validTrade = false;
                }
                if (validTrade) {
                    System.out.println(newPlayerTakingTrade);
                    System.out.println(newPlayerGivingTrade);
                    HashMap<ResourceType, Integer> newResources = new HashMap<>();
                    for (ResourceType resourceTaken : newPlayerTakingTrade.keySet()) {
                        if (newPlayerTakingTrade.get(resourceTaken) != 0)
                            newResources.put(resourceTaken, newPlayerTakingTrade.get(resourceTaken));
                    }

                    for (ResourceType resourceGiven : newPlayerGivingTrade.keySet()) {
                        if (newPlayerGivingTrade.get(resourceGiven) != 0)
                            newResources.put(resourceGiven, newPlayerGivingTrade.get(resourceGiven) * -1);
                    }

                    player.updatePlayerResourcesDict(newResources);
                    resetTrading();
                    acceptTradeButton.setVisible(false);
                    rejectTradeButton.setVisible(false);
                    gameClient.updateResources();
                    gameClient.acceptTradeOffer();
                }
            }
        });

        rejectTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tradeOffer = false;
                gamePanel.repaint();
                acceptTradeButton.setVisible(false);
                rejectTradeButton.setVisible(false);
            }
        });

        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildingNewSettlement = false;
                buildingNewRoad = false;
                upgradingToCity = false;
                tradingResources = false;

                rollDiceButton.setVisible(false);
                buildRoadButton.setVisible(false);
                buildSettlementButton.setVisible(false);
                upgradeSettlementButton.setVisible(false);
                tradeButton.setVisible(false);
                bankTradeButton.setVisible(false);
                playerTradeButton.setVisible(false);
                endTurnButton.setVisible(false);

                currentPlayerTurn = player.getPlayerNumber() < 4 ? player.getPlayerNumber() + 1 : 1;
                gameClient.updateTurn();
                gamePanel.repaint();
            }
        });

        discardCardsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int totalCards = 0;
                double totalToDiscard = 0;
                boolean discardValid = true;

                for (ResourceType resource : player.getPlayerResourcesDict().keySet()) {
                    totalCards += player.getPlayerResourcesDict().get(resource);
                    totalToDiscard += discardedCards.get(resource);
                    if (player.getPlayerResourcesDict().get(resource) < discardedCards.get(resource)) discardValid = false;
                }

                System.out.println(totalCards);
                System.out.println(totalToDiscard);

                if ((totalCards / totalToDiscard == 2 || (totalCards - 1) / totalToDiscard == 2) && discardValid) {
                    for (ResourceType resourceType : discardedCards.keySet())
                        discardedCards.replace(resourceType, discardedCards.get(resourceType) * -1);

                    player.updatePlayerResourcesDict(discardedCards);
                    System.out.println(player.getPlayerResourcesDict());
                    discardCards = false;
                    discardCardsButton.setVisible(false);
                    resetTrading();
                    gameClient.cardsDiscarded();
                }
            }
        });

        gamePanel.add(rollDiceButton);
        gamePanel.add(buildRoadButton);
        gamePanel.add(buildSettlementButton);
        gamePanel.add(upgradeSettlementButton);
        gamePanel.add(tradeButton);
        gamePanel.add(playerTradeButton);
        gamePanel.add(bankTradeButton);
        gamePanel.add(acceptTradeButton);
        gamePanel.add(rejectTradeButton);
        gamePanel.add(endTurnButton);
        gamePanel.add(discardCardsButton);
        gamePanel.add(diceRollLabel);
        gamePanel.add(thisPlayerScoreLabel);
        gamePanel.add(thisPlayerLabel);
        gamePanel.add(thisPlayerCardsLabel);
        gamePanel.add(currentPlayerLabel);
        gamePanel.add(player1Label);
        gamePanel.add(player2Label);
        gamePanel.add(player3Label);
        gamePanel.add(player1cardsLabel);
        gamePanel.add(player2cardsLabel);
        gamePanel.add(player3cardsLabel);
        gamePanel.add(moveRobberLabel);

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

    public void setButtonText() {
        buildSettlementButton.setText(buildingNewSettlement ? "Cancel Settlement" : "Build Settlement");
        buildRoadButton.setText(buildingNewRoad ? "Cancel Road" : "Build Road");
        upgradeSettlementButton.setText(upgradingToCity ? "Cancel Upgrade" : "Upgrade to City");

        if (tradingResources) {
            tradeButton.setText("Cancel Trade");
            playerTradeButton.setVisible(true);
            bankTradeButton.setVisible(true);
        }
        else {
            tradeButton.setText("Trade Resources");
            playerTradeButton.setVisible(false);
            bankTradeButton.setVisible(false);
        }
    }

    public void endOfGame() {
        finalScores = new StringBuilder();
        finalSettlements = new StringBuilder();
        finalCities = new StringBuilder();
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
        newGameButton.setVisible(true);
        waitingLabel.setVisible(false);

        diceRollLabel.setText("");
        thisPlayerScoreLabel.setText("");
        thisPlayerLabel.setText("");
        thisPlayerCardsLabel.setText("");
        currentPlayerLabel.setText("");
        player1Label.setText("");
        player2Label.setText("");
        player3Label.setText("");
        player1cardsLabel.setText("");
        player2cardsLabel.setText("");
        player3cardsLabel.setText("");
        moveRobberLabel.setText("");

        this.add(endgamePanel);
        this.remove(gamePanel);
        this.repaint();
    }

    public void startOfNewGame() {
        this.add(gamePanel);
        this.remove(endgamePanel);
        this.repaint();

        buildSettlementButton.setVisible(false);
        buildRoadButton.setVisible(false);
        rollDiceButton.setVisible(false);
        upgradeSettlementButton.setVisible(false);
        tradeButton.setVisible(false);
        playerTradeButton.setVisible(false);
        bankTradeButton.setVisible(false);
        endTurnButton.setVisible(false);
        diceRollLabel.setVisible(false);

        if (currentPlayerTurn == player.getPlayerNumber()) {
            buildSettlementButton.setVisible(true);
        }
    }

    public void resetTrading() {
        for (ResourceType resource : ResourceType.values()) {
            if (resource != ResourceType.DESERT) {
                currentPlayerTakingTrade.put(resource, 0);
                currentPlayerGivingTrade.put(resource, 0);
                discardedCards.put(resource, 0);
            }
        }
        acceptTradeButton.setVisible(false);
        rejectTradeButton.setVisible(false);
    }

    public void finishTrade() {
        HashMap<ResourceType, Integer> newResources = new HashMap<>();
        for (ResourceType resourceTaken : currentPlayerTakingTrade.keySet()) {
            if (currentPlayerTakingTrade.get(resourceTaken) != 0)
                newResources.put(resourceTaken, currentPlayerTakingTrade.get(resourceTaken));
        }
        for (ResourceType resourceGiven : currentPlayerGivingTrade.keySet()) {
            if (currentPlayerGivingTrade.get(resourceGiven) != 0)
                newResources.put(resourceGiven, currentPlayerGivingTrade.get(resourceGiven) * -1);
        }
        player.updatePlayerResourcesDict(newResources);
        resetTrading();
    }

    public void robPlayer(Tile tile) {
        playersToRob = new HashMap<>();
        for (ArrayList<Integer> node : tile.getCorrespondingNodeCoordinates()) {
            if (townsDict.containsKey(node) && townsDict.get(node).getTownColour() != player.getPlayerColour())
                playersToRob.put(townsDict.get(node).getTownColour(), new ArrayList<>(Arrays.asList(0, 0)));
        }
        robPlayer = !playersToRob.isEmpty();
        System.out.println(robPlayer);
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

        public void getResources() {
            csc.getResources();
        }

        public void offerPlayerTrade() {
            csc.offerPlayerTrade();
        }

        public void acceptTradeOffer() {
            csc.acceptTradeOffer();
        }

        public void endGame() {
            csc.endGame();
        }

        public void resetGame() {
            csc.resetGame();
        }

        public void sevenRolled() {
            csc.sevenRolled();
        }

        public void moveRobber() {
            csc.moveRobber();
        }

        public void playerRobbed() {
            csc.playerRobbed();
        }

        public void cardsDiscarded() {
            csc.cardsDiscarded();
        }

        public class ClientSideConnection {
            private Socket socket;
            private ObjectInputStream dataIn;
            private ObjectOutputStream dataOut;
            private int playerID;

            public ClientSideConnection() {
                System.out.println("---Client---");
                try {
                    socket = new Socket(ip_address, 44444);
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
                    dataOut.reset();
                    dataOut.writeObject(NEW_RESOURCES);
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from updateResources() CSC");
                }
            }

            public void getResources() {
                try {
                    dataOut.writeObject(GET_RESOURCES);
                    dataOut.writeInt(diceValue);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from getResources() CSC");
                }
            }

            public void offerPlayerTrade() {
                try {
                    dataOut.writeObject(NEW_TRADE);
                    dataOut.writeObject(currentPlayerTakingTrade);
                    dataOut.writeObject(currentPlayerGivingTrade);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from offerPlayerTrade() CSC");
                }
            }

            public void acceptTradeOffer() {
                try {
                    dataOut.writeObject(NEW_TRADE_ACCEPTED);
                    dataOut.writeObject(newPlayerTakingTrade);
                    dataOut.writeObject(newPlayerGivingTrade);
                    dataOut.reset();
                    dataOut.writeObject(tradingPlayer);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from acceptTradeOffer() CSC");
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
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from resetGame() CSC");
                }
            }

            public void sevenRolled() {
                try {
                    dataOut.writeObject(SEVEN_ROLLED);
                    dataOut.reset();
                    dataOut.writeObject(allPlayers);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from sevenRolled() CSC");
                }
            }

            public void moveRobber() {
                try {
                    dataOut.writeObject(ROBBER_MOVED);
                    dataOut.writeObject(tilesDict);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred from moveRobber() CSC");
                }
            }

            public void playerRobbed() {
                try {
                    dataOut.writeObject(PLAYER_ROBBED);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.writeObject(robbedPlayer);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred in playerRobbed() CSC");
                }
            }

            public void cardsDiscarded() {
                try {
                    dataOut.writeObject(CARDS_DISCARDED);
                    dataOut.reset();
                    dataOut.writeObject(player);
                    dataOut.flush();
                } catch (IOException e) {
                    System.out.println("IOException occurred in cardsDiscarded() CSC");
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
                                        tradeOffer = false;
                                        acceptTradeButton.setVisible(false);
                                        rejectTradeButton.setVisible(false);
                                        if (currentPlayerTurn == player.getPlayerNumber()) {
                                            if (gameState == GameState.INITIAL_PLACEMENT) {
                                                buildSettlementButton.setVisible(true);
                                            }
                                            if (gameState == GameState.NORMAL_PLAY) {
                                                rollDiceButton.setVisible(true);
                                                buildRoadButton.setVisible(false);
                                                buildSettlementButton.setVisible(false);
                                                upgradeSettlementButton.setVisible(false);
                                                tradeButton.setVisible(false);
                                                endTurnButton.setVisible(false);
                                            }
                                        }
                                        displayUserStats();
                                        break;
                                    case NEW_RESOURCES_ADDED:
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        if (currentPlayerTurn == player.getPlayerNumber()) {
                                            if (gameState == GameState.NORMAL_PLAY) {
                                                rollDiceButton.setVisible(false);
                                                buildRoadButton.setVisible(true);
                                                buildSettlementButton.setVisible(true);
                                                upgradeSettlementButton.setVisible(true);
                                                tradeButton.setVisible(true);
                                                endTurnButton.setVisible(true);
                                            }
                                        }
                                        displayUserStats();
                                        break;
                                    case GET_RESOURCES_ADDED:
                                        diceValue = dataIn.readInt();
                                        HashMap<ArrayList<Integer>, Town> playerTownsDict = player.getPlayerTownsDict();

                                        diceRollLabel.setVisible(true);
                                        diceRollLabel.setText("Dice Roll: " + diceValue);

                                        if (diceValue != 7) {
                                            newResources = new HashMap<>();
                                            for (Tile tile : tilesDict.values()) {
                                                for (ArrayList<Integer> tileNode : tile.getCorrespondingNodeCoordinates()) {
                                                    for (ArrayList<Integer> town : playerTownsDict.keySet()) {
                                                        if (playerTownsDict.get(town).getTownCoordinates().equals(tileNode) &&
                                                                tile.getRollValue() == diceValue && !tile.isTileBlocked()) {
                                                            int resourceAmount = playerTownsDict.get(town).isCity() ? 2 : 1;
                                                            if (newResources.containsKey(tile.getTileResource()))
                                                                newResources.put(tile.getTileResource(), newResources.get(tile.getTileResource()) + resourceAmount);
                                                            else
                                                                newResources.put(tile.getTileResource(), resourceAmount);
                                                        }
                                                    }
                                                }
                                            }
                                            player.updatePlayerResourcesDict(newResources);
                                            updateResources();
                                        }
                                        break;
                                    case NEW_CITY_ADDED:
                                        townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        displayUserStats();
                                        break;
                                    case NEW_TRADE_ADDED:
                                        newPlayerGivingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                        newPlayerTakingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                        tradingPlayer = (Player) dataIn.readObject();
                                        tradeOffer = true;
                                        System.out.println("Taking");
                                        System.out.println(newPlayerTakingTrade);
                                        System.out.println("Giving");
                                        System.out.println(newPlayerGivingTrade);
                                        break;
                                    case ACCEPTED_TRADE_ADDED:
                                        currentPlayerTakingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                        currentPlayerGivingTrade = (HashMap<ResourceType, Integer>) dataIn.readObject();
                                        tradingPlayer = (Player) dataIn.readObject();
                                        if (player.getPlayerNumber() == tradingPlayer.getPlayerNumber()) {
                                            finishTrade();
                                            updateResources();
                                        }
                                        tradeOffer = false;
                                        acceptTradeButton.setVisible(false);
                                        rejectTradeButton.setVisible(false);
                                        resetTrading();
                                        break;
                                    case END_GAME_ADDED:
                                        gameState = (GameState) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        endOfGame();
                                        break;
                                    case RESET_PLAYERS:
                                        gameState = (GameState) dataIn.readObject();
                                        tilesDict = (HashMap<ArrayList<Integer>, Tile>) dataIn.readObject();
                                        nodesDict = (HashMap<ArrayList<Integer>, Node>) dataIn.readObject();
                                        townsDict = (HashMap<ArrayList<Integer>, Town>) dataIn.readObject();
                                        roadsDict = (HashMap<ArrayList<ArrayList<Integer>>, Road>) dataIn.readObject();
                                        currentPlayerTurn = dataIn.readInt();
                                        player = new Player(player.getPlayerColour(), player.getPlayerNumber());
                                        sendPlayer();
                                        startOfNewGame();
                                        break;
                                    case ROBBER_MOVE_ADDED:
                                        tilesDict = (HashMap<ArrayList<Integer>, Tile>) dataIn.readObject();
                                        break;
                                    case PLAYER_ROB_ADDED:
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        for (Player p : allPlayers) {
                                            if (p.getPlayerColour() == player.getPlayerColour()) {
                                                player = p;
                                                break;
                                            }
                                        }
                                        displayUserStats();
                                        break;
                                    case SEVEN_ROLLED_ADDED:
                                        if (player.getPlayerNumber() == currentPlayerTurn) {
                                            moveRobber = true;
                                            moveRobberLabel.setText("Move Robber");
                                        }
                                        break;
                                    case CARDS_TO_DISCARD_ADDED:
                                        gameState = (GameState) dataIn.readObject();
                                        discardingPlayers = (ArrayList<Player>) dataIn.readObject();
                                        for (Player discardingPlayer : discardingPlayers) {
                                            if (discardingPlayer.getPlayerNumber() == player.getPlayerNumber()) {
                                                discardCards = true;
                                                discardCardsButton.setVisible(true);
                                            }
                                        }
                                        break;
                                    case CARDS_DISCARDED_ADDED:
                                        gameState = (GameState) dataIn.readObject();
                                        allPlayers = (ArrayList<Player>) dataIn.readObject();
                                        if (currentPlayerTurn == player.getPlayerNumber()) {
                                            moveRobber = true;
                                            moveRobberLabel.setText("Move Robber");
                                        }
                                        displayUserStats();
                                        break;
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
                        int totalCards = 0;
                        for (Integer noResources : player.getPlayerResourcesDict().values()) {
                            totalCards += noResources;
                        }
                        thisPlayerCardsLabel.setText("Your No. of Cards: " + totalCards);
                    }
                    else {
                        int totalCards = 0;
                        for (Integer resourceAmount : currentPlayer.getPlayerResourcesDict().values()) {
                            totalCards += resourceAmount;
                        }

                        switch (counter) {
                            case 0:
                                player1Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                player1cardsLabel.setText(currentPlayer.getPlayerColour() + "'s no. of cards: " + totalCards);
                                counter++;
                                break;
                            case 1:
                                player2Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                player2cardsLabel.setText(currentPlayer.getPlayerColour() + "'s no. of cards: " + totalCards);
                                counter++;
                                break;
                            case 2:
                                player3Label.setText(currentPlayer.getPlayerColour() + "'s Score: " + currentPlayer.getScore());
                                player3cardsLabel.setText(currentPlayer.getPlayerColour() + "'s no. of cards: " + totalCards);
                                counter++;
                                break;
                        }
                    }
                }
            }
        }
    }
}
