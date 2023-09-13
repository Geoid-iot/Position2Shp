package com.example.position2shp.Map;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public enum TrackingColour {
    RED(Color.RED), YELLOW(Color.YELLOW), GREEN(Color.GREEN), BLUE(Color.BLUE), TRANSPERENT(Color.TRANSPARENT);

    private int numVal;

    TrackingColour(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }

    private static final Map<Integer, TrackingColour> valueMap = new HashMap<>();

    static {
        for (TrackingColour color : values()) {
            valueMap.put(color.numVal, color);
        }
    }

    public static TrackingColour fromValue(Integer numVal) {
        return valueMap.get(numVal);
    }
}
