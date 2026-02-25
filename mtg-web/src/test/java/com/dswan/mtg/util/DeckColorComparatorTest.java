package com.dswan.mtg.util;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.Deck;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DeckColorComparatorTest {

    private final DeckColorComparator comparator = new DeckColorComparator();

    @Test
    void sortsDecksByAggregatedColorIdentity() {
        Deck monoWhite = deckWithColors("W");
        Deck monoRed = deckWithColors("R");
        Deck dimir = deckWithColors("U", "B");
        Deck fiveColor = deckWithColors("W", "U", "B", "R", "G");

        List<Deck> decks = Arrays.asList(fiveColor, dimir, monoRed, monoWhite);

        decks.sort(comparator);

        assertThat(decks)
                .extracting(this::normalized)
                .containsExactly(
                        List.of("W"),                  // mono-white
                        List.of("R"),                  // mono-red
                        List.of("U", "B"),                // Dimir
                        List.of("W", "U", "B", "R", "G")  // 5-color
                );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Deck deckWithColors(String... colors) {
        Deck deck = new Deck();

        List<Card> cards = Stream.of(colors)
                .map(this::cardWithColor)
                .toList();

        deck.setCards(cards);
        return deck;
    }

    private Card cardWithColor(String color) {
        Card card = new Card();
        card.setColorIdentity(List.of(color));
        return card;
    }

    private List<String> normalized(Deck deck) {
        return deck.getCards().stream()
                .flatMap(c -> c.getColorIdentity().stream())
                .distinct()
                .sorted(new CardColorComparator())
                .toList();
    }
}