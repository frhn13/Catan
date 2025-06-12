package gameVisualisation;

import gameObjects.GameBoard;
import gameObjects.Node;
import gameObjects.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import static Constants.Constants.*;

public class MainGame extends JFrame implements ActionListener, MouseListener {
    JButton rollDiceButton;
    JButton buildRoadButton;
    JButton buildSettlementButton;
    JButton upgradeSettlementButton;

    public MainGame() {
        JPanel gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
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
                        Image tileImg = new ImageIcon(getClass().getResource("/Images/" + tilesDict.get(tile).getTileResource().image)).getImage();
                        g.drawImage(tileImg, tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 150 + 50, TILE_WIDTH, TILE_HEIGHT, null);
                        //g.fillRect(tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x, tilesDict.get(tile).getTileCoordinates().getLast() * 200 + 50, 100, 100);
                        g.setFont(new Font("Arial", Font.BOLD, 50));
                        g.setColor(Color.white);
                        g.drawString(String.valueOf(tilesDict.get(tile).getRollValue()), tilesDict.get(tile).getTileCoordinates().getFirst() * 200 + base_x + 80, tilesDict.get(tile).getTileCoordinates().getLast() * 150 + 150);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                HashMap<ArrayList<Integer>, Node> nodesDict = GameBoard.getNodesDict();
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
                }

            }
        };

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

        rollDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("A");
            }
        });

        buildRoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("B");
            }
        });

        buildSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("C");
            }
        });

        upgradeSettlementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("D");
            }
        });

        gamePanel.add(rollDiceButton);
        gamePanel.add(buildRoadButton);
        gamePanel.add(buildSettlementButton);
        gamePanel.add(upgradeSettlementButton);

        this.setSize(DEFAULT_GAME_WIDTH, DEFAULT_GAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        //this.pack();
        this.add(gamePanel);
        this.setVisible(true);
        //System.out.println(rollDiceButton.isEnabled());
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
}
