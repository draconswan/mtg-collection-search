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

    public static DeckEntity toEntity(Deck domain) {
        if (domain == null) {
            return null;
        }
        DeckEntity entity = new DeckEntity();
        entity.setId(domain.getId());
        entity.setDeckName(domain.getName());
        entity.setDeckType(domain.getType());
        entity.setCreatedAt(domain.getCreatedAt() != null
                ? domain.getCreatedAt().toOffsetDateTime()
                : null);

        entity.setLastUpdated(domain.getLastUpdated() != null
                ? domain.getLastUpdated().toOffsetDateTime()
                : null);
        if (domain.getCards() != null && !domain.getCards().isEmpty()) {
            List<DeckCardEntity> deckCardEntities = domain.getCards().stream()
                    .map(card -> {
                        DeckCardEntity dce = new DeckCardEntity();
                        dce.setDeckEntity(entity);
                        dce.setCard(CardMapper.toEntity(card));
                        DeckCardId id = new DeckCardId();
                        id.setDeckId(entity.getId());
                        id.setCardId(card.getId());
                        dce.setId(id);
                        dce.setQuantity(card.getQuantity());
                        dce.setChecked(card.isChecked());
                        return dce;
                    })
                    .collect(Collectors.toList());
            entity.setCards(deckCardEntities);
        } else {
            entity.setCards(new ArrayList<>());
        }
        return entity;
    }
}