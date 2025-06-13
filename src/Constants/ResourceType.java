package Constants;

import java.awt.*;

public enum ResourceType {
    LUMBER(new Color(110, 38, 14), "lumber_tile.png", "lumber_card.jpg"),
    GRAIN(Color.yellow, "grain_tile.png", "grain_card.jpg"),
    BRICK(Color.red, "brick_tile.png", "brick_card.jpg"),
    WOOL(Color.white, "wool_tile.png", "wool_card.png"),
    ORE(Color.gray, "ore_tile.png", "ore_card.jpg"),
    DESERT(Color.orange, "desert_tile.png", "");

    public final Color colour;
    public final String tileImage;
    public final String cardImage;

    ResourceType(Color colour, String tileImage, String cardImage) {
        this.colour = colour;
        this.tileImage = tileImage;
        this.cardImage = cardImage;
    }
}
