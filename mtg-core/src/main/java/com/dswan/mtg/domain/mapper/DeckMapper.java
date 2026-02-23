package com.dswan.mtg.domain.mapper;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.entity.DeckCardEntity;
import com.dswan.mtg.domain.entity.DeckCardId;
import com.dswan.mtg.domain.entity.DeckEntity;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeckMapper {
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static Deck toDomain(DeckEntity entity) {
        if (entity == null) {
            return null;
        }
        Deck deck = new Deck();
        deck.setId(entity.getId());
        deck.setName(entity.getDeckName());
        deck.setType(entity.getDeckType());
        deck.setCreatedAt(entity.getCreatedAt() != null
                ? entity.getCreatedAt().atZoneSameInstant(UTC)
                : null);
        deck.setLastUpdated(entity.getLastUpdated() != null
                ? entity.getLastUpdated().atZoneSameInstant(UTC)
                : null);
        if (entity.getCards() != null && !entity.getCards().isEmpty()) {
            List<Card> domainCards = entity.getCards().stream()
                    .map(dc -> {
                        Card card = CardMapper.toDomain(dc.getCard());
                        card.setQuantity(dc.getQuantity());
                        card.setChecked(dc.getChecked());
                        return card;
                    })
                    .collect(Collectors.toList());
            deck.setCards(domainCards);
        } else {
            deck.setCards(new ArrayList<>());
        }
        return deck;
    }

    public static DeckEntity toNewEntity(Deck domain) {
        DeckEntity entity = new DeckEntity();
        entity.setDeckName(domain.getName());
        entity.setDeckType(domain.getType());
        entity.setCreatedAt(domain.getCreatedAt() != null
                ? domain.getCreatedAt().toOffsetDateTime()
                : null);
        entity.setLastUpdated(domain.getLastUpdated() != null
                ? domain.getLastUpdated().toOffsetDateTime()
                : null);
        return entity;
    }

    public static void updateEntity(DeckEntity entity, Deck domain) {
        entity.setDeckName(domain.getName());
        entity.setDeckType(domain.getType());
        entity.setLastUpdated(domain.getLastUpdated() != null
                ? domain.getLastUpdated().toOffsetDateTime()
                : null);
    }

    public static void syncCards(DeckEntity entity, Deck domain) {
        // Remove cards not in incoming deck
        entity.getCards().removeIf(existing ->
                domain.getCards().stream()
                        .noneMatch(c -> c.getId().equals(existing.getCard().getId()))
        );
        // Add or update incoming cards
        for (Card card : domain.getCards()) {
            entity.getCards().stream()
                    .filter(e -> e.getCard().getId().equals(card.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            e -> {
                                e.setQuantity(card.getQuantity());
                                e.setChecked(card.isChecked());
                            },
                            () -> {
                                DeckCardEntity newCard = new DeckCardEntity();
                                newCard.setDeckEntity(entity);
                                newCard.setCard(CardMapper.toEntity(card));

                                DeckCardId id = new DeckCardId();
                                id.setDeckId(entity.getId());
                                id.setCardId(card.getId());
                                newCard.setId(id);

                                newCard.setQuantity(card.getQuantity());
                                newCard.setChecked(card.isChecked());

                                entity.getCards().add(newCard);
                            }
                    );
        }
    }
}