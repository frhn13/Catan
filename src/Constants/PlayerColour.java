package Constants;

import java.awt.*;

public enum PlayerColour {
    RED(Color.red),
    ORANGE(Color.orange),
    GREEN(Color.green),
    BLUE(Color.blue);

    public final Color colour;

    PlayerColour(Color colour) {
        this.colour = colour;
    }
}
