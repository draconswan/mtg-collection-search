package com.dswan.mtg.domain.cards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DeckFormat {

    DEFAULT(0,0,0,0,
            false, false, false, false,
            List.of(DeckZone.MAINBOARD)),

    // Commander-family formats (no sideboard)
    COMMANDER(100, 1, 2, 0,
            false, true, true, false,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD, DeckZone.COMPANION_ZONE)
    ),

    PAUPER_COMMANDER(100, 1, 1, 0,
            false, true, true, false,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD, DeckZone.COMPANION_ZONE)
    ),

    DUEL(100, 1, 1, 0,
            false, true, true, false,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD, DeckZone.COMPANION_ZONE)
    ),

    PREDH(100, 1, 2, 0,
            false, true, true, false,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD, DeckZone.COMPANION_ZONE)
    ),

    BRAWL(60, 1, 1, 0,
            false, true, true, false,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD, DeckZone.COMPANION_ZONE)
    ),

    OATHBREAKER(60, 2, 2, 0,
            false, false, false, true,
            List.of(DeckZone.COMMAND_ZONE, DeckZone.MAINBOARD)
    ),

    // Constructed formats (sideboard + companion)
    STANDARD(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    PIONEER(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    MODERN(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    LEGACY(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    VINTAGE(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    PAUPER(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    PENNY(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    PREMODERN(60, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    ),

    GLADIATOR(100, 0, 0, 15,
            true, true, false, false,
            List.of(DeckZone.MAINBOARD, DeckZone.SIDEBOARD, DeckZone.COMPANION_ZONE)
    );

    // --- Properties ---
    public final int totalDeckSize;
    public final int minCommandZoneSize;
    public final int maxCommandZoneSize;
    public final int sideboardSize;
    public final boolean hasSideboard;
    public final boolean hasCompanionZone;
    public final boolean companionConsumesMainboardSlot;
    public final boolean hasSignatureSpell;
    public final List<DeckZone> zones;

    private static final Map<String, DeckFormat> LOOKUP;

    static {
        LOOKUP = new HashMap<>();
        for (DeckFormat format : values()) {
            LOOKUP.put(normalize(format.name()), format);
        }
    }

    private static String normalize(String s) {
        return s.toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }

    public static DeckFormat fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        DeckFormat format = LOOKUP.get(normalize(raw));
        if (format == null) {
            throw new IllegalArgumentException("Unknown deck format: " + raw);
        }
        return format;
    }

    DeckFormat(int totalDeckSize,
               int minCommandZoneSize,
               int maxCommandZoneSize,
               int sideboardSize,
               boolean hasSideboard,
               boolean hasCompanionZone,
               boolean companionConsumesMainboardSlot,
               boolean hasSignatureSpell,
               List<DeckZone> zones) {
        this.totalDeckSize = totalDeckSize;
        this.minCommandZoneSize = minCommandZoneSize;
        this.maxCommandZoneSize = maxCommandZoneSize;
        this.sideboardSize = sideboardSize;
        this.hasSideboard = hasSideboard;
        this.hasCompanionZone = hasCompanionZone;
        this.companionConsumesMainboardSlot = companionConsumesMainboardSlot;
        this.hasSignatureSpell = hasSignatureSpell;
        this.zones = zones;
    }

    public int getMainboardSize(int commandZoneCount, boolean hasCompanion) {
        int companionAdjustment = (hasCompanion && companionConsumesMainboardSlot) ? 1 : 0;
        return totalDeckSize - commandZoneCount - companionAdjustment;
    }
}
