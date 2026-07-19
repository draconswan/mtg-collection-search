package com.dswan.mtg.domain.cards;

import java.util.List;

public class FormatZoneUtil {

    public static List<DeckZone> zonesFor(String format) {
        String f = format.toLowerCase();

        // Commander-family formats (no sideboard)
        if (f.equals("commander") ||
            f.equals("pauper commander") ||
            f.equals("duel") ||                // Duel Commander
            f.equals("brawl")) {
            return List.of(
                    DeckZone.COMMAND_ZONE,
                    DeckZone.MAINBOARD,
                    DeckZone.COMPANION_ZONE
            );
        }

        // Oathbreaker (special case)
        if (f.equals("oathbreaker")) {
            return List.of(
                    DeckZone.COMMAND_ZONE,
                    DeckZone.MAINBOARD
            );
        }

        // Constructed formats with sideboard + companion
        if (List.of(
                "standard", "pioneer", "modern", "legacy", "vintage",
                "pauper", "premodern",
                "historic", "timeless", "alchemy",
                "gladiator", "penny dreadful"
        ).contains(f)) {
            return List.of(
                    DeckZone.MAINBOARD,
                    DeckZone.SIDEBOARD,
                    DeckZone.COMPANION_ZONE
            );
        }

        // Fallback
        return List.of(DeckZone.MAINBOARD);
    }
}
