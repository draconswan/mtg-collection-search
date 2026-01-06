package com.dswan.mtg.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CardColorComparator implements Comparator<String> {

    private static final List<String> COLOR_ORDER = Arrays.asList(
            "W", "U", "B", "R", "G"
    );

    @Override
    public int compare(String color1, String color2) {
        int index1 = getColorIndex(color1);
        int index2 = getColorIndex(color2);
        return Integer.compare(index1, index2);
    }

    private int getColorIndex(String color) {
        if (color == null) return COLOR_ORDER.size() + 1; // Treat null as lowest priority
        if (color.length() > 1) return COLOR_ORDER.size(); // Multi-color cards come after mono-colored ones
        int index = COLOR_ORDER.indexOf(color);
        return index >= 0 ? index : COLOR_ORDER.size() + 2; // Unknown colors go last
    }
}