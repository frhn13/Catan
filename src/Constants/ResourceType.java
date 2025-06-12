package Constants;

import java.awt.*;

public enum ResourceType {
    LUMBER(new Color(110, 38, 14)),
    GRAIN(Color.yellow),
    BRICK(Color.red),
    WOOL(Color.white),
    ORE(Color.gray),
    DESERT(Color.orange);

    public final Color colour;

    ResourceType(Color colour) {
        this.colour = colour;
    }
}
