package org.dynamics.model;

import java.awt.*;

public enum Corner {
    RED(new Color(245,134,133)),BLUE(new Color(173, 216, 230));
    private final Color color;
    Corner(Color color){
        this.color = color;
    }
    public Color getColor() {
        return  this.color;
    }
}
