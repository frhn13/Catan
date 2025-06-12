package gameVisualisation;

import gameObjects.GameBoard;
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

    public MainGame() {

        JPanel gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                HashMap<ArrayList<Integer>, Tile> tilesDict = GameBoard.getTilesDict();
                for (ArrayList<Integer> tile : GameBoard.getTilesDict().keySet()) {
                    g.setColor(GameBoard.getTilesDict().get(tile).getTileResource().colour);
                    g.fillRect(GameBoard.getTilesDict().get(tile).getTileCoordinates().getFirst()*200 + 50, GameBoard.getTilesDict().get(tile).getTileCoordinates().getLast()*200 + 50, 100, 100);
                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.black);
                    g.drawString(String.valueOf(GameBoard.getTilesDict().get(tile).getRollValue()), GameBoard.getTilesDict().get(tile).getTileCoordinates().getFirst()*200 + 50, GameBoard.getTilesDict().get(tile).getTileCoordinates().getLast()*200 + 50);
                    drawTile();
                }
            }

            public void drawTile() {

            }
        };

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
}
