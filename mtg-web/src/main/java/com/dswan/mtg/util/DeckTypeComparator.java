package com.dswan.mtg.util;

import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.model.HasDeckType;

import java.util.*;

public class DeckTypeComparator<T extends HasDeckType> implements Comparator<T> {

    // Flatten all formats into a single ordered list
    private static final List<String> ORDERED_FORMATS = DeckFormats.FORMATS.values().stream()
            .flatMap(List::stream)
            .toList();

    // Map format → index for fast lookup
    private static final Map<String, Integer> FORMAT_INDEX;

    static {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < ORDERED_FORMATS.size(); i++) {
            indexMap.put(ORDERED_FORMATS.get(i), i);
        }
        FORMAT_INDEX = Collections.unmodifiableMap(indexMap);
    }

    @Override
    public int compare(T d1, T d2) {
        String f1 = d1.getDeckType();
        String f2 = d2.getDeckType();

        Integer i1 = FORMAT_INDEX.get(f1);
        Integer i2 = FORMAT_INDEX.get(f2);

        boolean f1Known = i1 != null;
        boolean f2Known = i2 != null;

        // Known formats always come before unknown formats
        if (f1Known || f2Known) {
            if (f1Known && f2Known) {
                return Integer.compare(i1, i2);
            }
            return f1Known ? -1 : 1;
        }

        // Both unknown → alphabetical fallback
        return f1.compareToIgnoreCase(f2);
    }
}
