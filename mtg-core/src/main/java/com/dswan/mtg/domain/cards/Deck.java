package com.dswan.mtg.domain.cards;

import com.dswan.mtg.util.CardColorComparator;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Data
public class Deck {
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

    public void calculateDeckColors() {
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
}
