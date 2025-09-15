package com.dswan.mtg.domain;

import java.util.Objects;

public enum CardType {
    LAND,
    CREATURE,
    ARTIFACT,
    ENCHANTMENT,
    PLANESWALKER,
    BATTLE,
    INSTANT,
    SORCERY,
    KINDRED,
    DUNGEON,
    TOKEN,
    OTHER;

    public static CardType fromString(String value) {
        try {
            return CardType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            if (Objects.equals(value, "TRIBAL")) {
                return KINDRED;
            }
            return OTHER;
        }
    }
}
