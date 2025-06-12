package Constants;

import java.awt.*;

public enum ResourceType {
    LUMBER(new Color(110, 38, 14), "lumber_tile.png"),
    GRAIN(Color.yellow, "grain_tile.png"),
    BRICK(Color.red, "brick_tile.png"),
    WOOL(Color.white, "wool_tile.png"),
    ORE(Color.gray, "ore_tile.png"),
    DESERT(Color.orange, "desert_tile.png");

    public final Color colour;
    public final String image;

    ResourceType(Color colour, String image) {
        this.colour = colour;
        this.image = image;
    }
}
