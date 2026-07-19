package com.dswan.mtg.domain.cards;

import lombok.Getter;

@Getter
public enum DeckZone {
    COMMAND_ZONE("Command Zone"),
    MAINBOARD("Mainboard"),
    SIDEBOARD("Sideboard"),
    COMPANION_ZONE("Companion");

    private final String displayName;

    DeckZone(String displayName) {
        this.displayName = displayName;
    }

    public static DeckZone fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Zone cannot be null");
        }

        String normalized = raw.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        return DeckZone.valueOf(normalized);
    }

    public static DeckZone fromNullableString(String raw) {
        if (raw == null) {
            return DeckZone.MAINBOARD;
        }
        return fromString(raw);
    }
}
