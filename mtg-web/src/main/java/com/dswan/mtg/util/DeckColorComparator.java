package com.dswan.mtg.util;

import com.dswan.mtg.domain.cards.Deck;

import java.util.Comparator;
import java.util.List;

public class DeckColorComparator implements Comparator<Deck> {

    private static final CardColorComparator CARD_COLOR_COMPARATOR = new CardColorComparator();

    @Override
    public int compare(Deck d1, Deck d2) {
        List<String> c1 = d1.getDeckColors();
        List<String> c2 = d2.getDeckColors();
        boolean d1Colorless = c1.size() == 1 && c1.contains("C");
        boolean d2Colorless = c2.size() == 1 && c2.contains("C");

        // Colorless decks always come last
        if (d1Colorless || d2Colorless) {
            return d1Colorless == d2Colorless ? 0 : (d1Colorless ? 1 : -1);
        }

        // Compare by number of colors first (mono < two-color < three-color...)
        int sizeCompare = Integer.compare(c1.size(), c2.size());
        if (sizeCompare != 0) {
            return sizeCompare;
        }

        // Compare lexicographically in WUBRG order
        for (int i = 0; i < c1.size(); i++) {
            int cmp = CARD_COLOR_COMPARATOR.compare(c1.get(i), c2.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }
}