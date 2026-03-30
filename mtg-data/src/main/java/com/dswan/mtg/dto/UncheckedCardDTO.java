package com.dswan.mtg.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UncheckedCardDTO(
        UUID deckId,
        String deckName,
        UUID cardId,
        String oracleId,
        String cardName,
        String printedName,
        String flavorName,
        String lang,
        String setCode,
        String setName,
        LocalDate releasedAt,
        String typeLine,
        String manaCost,
        Long quantity
) {
    public String displayName() {
        if (lang != null && !lang.equals("en")) {
            return cardName;
        }
        if (printedName != null && !printedName.isBlank()) {
            return printedName;
        }
        if (flavorName != null && !flavorName.isBlank()) {
            return flavorName + " (" + cardName + ")";
        }
        return cardName;
    }
}