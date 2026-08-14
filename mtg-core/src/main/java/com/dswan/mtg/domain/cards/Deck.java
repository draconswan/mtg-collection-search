package com.dswan.mtg.domain.cards;

import com.dswan.mtg.domain.model.HasDeckColors;
import com.dswan.mtg.domain.model.HasDeckType;
import com.dswan.mtg.util.CardColorComparator;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class Deck implements HasDeckColors, HasDeckType {
    private String id;
    private String name;
    private String type;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastUpdated;
    private List<Card> cards;
    private List<String> deckColors;

    public List<String> getDeckColors() {
        if (CollectionUtils.isEmpty(deckColors)) {
            calculateDeckColors();
        }
        return deckColors;
    }

    @Override
    public String getDeckType() {
        return type;
    }

    public int getCheckedCount() {
        return cards.stream()
                .filter(Card::isChecked)
                .mapToInt(Card::getQuantity)
                .sum();
    }

    public int getUncheckedCount() {
        return cards.stream()
                .filter(c -> !c.isChecked())
                .mapToInt(Card::getQuantity)
                .sum();
    }

    public int getTotalCards() {
        return cards.stream()
                .mapToInt(Card::getQuantity)
                .sum();
    }

    public void calculateDeckColors() {
        if (this.cards == null) {
            this.cards = new ArrayList<>();
        }
        List<String> rawColors = this.cards.stream()
                .flatMap(card -> {
                    List<String> ci = card.getColorIdentity();
                    if (ci == null || ci.isEmpty()) {
                        return Stream.of("C");
                    }
                    return ci.stream();
                })
                .distinct()
                .toList();

        List<String> filtered =
                rawColors.size() > 1
                        ? rawColors.stream().filter(c -> !c.equals("C")).toList()
                        : rawColors;

        this.deckColors = filtered.stream()
                .sorted(new CardColorComparator())
                .toList();
    }

    public Optional<Card> getCard(String cardId) {
        if (cards == null) return Optional.empty();
        return cards.stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst();
    }

    public Optional<Card> findCardInZone(String cardId, DeckZone zone) {
        if (cards == null) return Optional.empty();
        return cards.stream()
                .filter(c -> c.getId().equals(cardId) && DeckZone.fromString(c.getLocation()) == zone)
                .findFirst();
    }

    public void addCard(Card card) {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        cards.add(card);
    }

    public void removeCard(Card card) {
        if (cards != null) {
            cards.remove(card);
        }
    }
}
