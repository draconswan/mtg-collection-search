package com.dswan.mtg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardTypeInfo {
    private CardSupertype supertype;
    @NonNull
    private List<CardType> cardType;
    private List<String> subtypes;

    public static CardTypeInfo fromTypeLine(String typeLine) {
        if (typeLine == null || typeLine.isBlank()) {
            return new CardTypeInfo(null, List.of(), List.of());
        }

        String[] parts = typeLine.split("—"); // em dash
        String left = parts[0].trim(); // e.g. "Legendary Creature"
        String right = parts.length > 1 ? parts[1].trim() : ""; // e.g. "Elf Druid"

        // Parse supertype and card types
        List<String> leftTokens = Arrays.asList(left.split("\\s+"));
        CardSupertype supertype = null;
        List<CardType> cardTypes = new ArrayList<>();

        for (String token : leftTokens) {
            try {
                CardType type = CardType.valueOf(token.toUpperCase());
                cardTypes.add(type);
            } catch (IllegalArgumentException e) {
                try {
                    supertype = CardSupertype.valueOf(token.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    // Unknown token, skip
                }
            }
        }

        // Parse subtypes
        List<String> subtypes = right.isEmpty()
                ? List.of()
                : Arrays.stream(right.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return new CardTypeInfo(supertype, cardTypes, subtypes);

    }
}