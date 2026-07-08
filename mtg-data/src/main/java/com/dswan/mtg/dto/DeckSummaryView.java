package com.dswan.mtg.dto;

import com.dswan.mtg.domain.model.HasDeckColors;
import com.dswan.mtg.domain.model.HasDeckType;

import java.util.List;
import java.util.UUID;

public record DeckSummaryView(
        UUID id,
        String name,
        String type,
        Long totalCards,
        Long checkedCount,
        Long proxyCount,
        String deckColors,
        String badgeClass
) implements HasDeckColors, HasDeckType {

    @Override
    public List<String> getDeckColors() {
        return deckColors == null ? List.of() : List.of(deckColors.split(""));
    }

    @Override
    public String getDeckType() {
        return type;
    }

    public String computeBadgeClass(DeckSummaryView deck) {
        if (deck.type().equalsIgnoreCase("Commander") && deck.totalCards() != 100) {
            return "bg-danger";
        }
        if (!deck.type().equalsIgnoreCase("Commander") && deck.totalCards() != 60) {
            return "bg-danger";
        }
        if (deck.checkedCount() == 0) {
            return "bg-secondary";
        }
        if (deck.checkedCount().equals(deck.totalCards())) {
            return deck.proxyCount() > 0
                    ? "bg-primary"
                    : "bg-success";
        }
        return "bg-info";
    }
}
