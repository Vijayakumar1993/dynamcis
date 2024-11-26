package org.dynamics.model;

import org.checkerframework.checker.units.qual.C;

import java.awt.*;

public enum Corner {
    RED(Color.RED),BLUE(Color.BLUE);
    private final Color color;
    Corner(Color color){
        this.color = color;
    }
    public Color getColor() {
        return  this.color;
    }
}
