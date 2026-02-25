package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.domain.mapper.DeckMapper;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckBuilderService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    public Deck buildDeck(DeckStateForm deckStateForm) {
        Deck deck;
        if (deckStateForm.getDeckId() != null) {
            UUID deckId = isValidUUID(deckStateForm.getDeckId())
                    ? UUID.fromString(deckStateForm.getDeckId())
                    : UUID.randomUUID();
            deckStateForm.setDeckId(deckId.toString());
            Optional<DeckEntity> existingDeck = deckRepository.findById(UUID.fromString(deckStateForm.getDeckId()));
            if (existingDeck.isPresent()) {
                deck = DeckMapper.toDomain(existingDeck.get());
                deck.setName(deckStateForm.getDeckName());
                deck.setType(deckStateForm.getDeckFormat());
            } else {
                deck = new Deck();
                deck.setName(deckStateForm.getDeckName());
                deck.setType(deckStateForm.getDeckFormat());
                deck.setCreatedAt(ZonedDateTime.now());
            }
        } else {
            deck = new Deck();
            deck.setName(deckStateForm.getDeckName());
            deck.setType(deckStateForm.getDeckFormat());
            deck.setCreatedAt(ZonedDateTime.now());
        }
        deck.setLastUpdated(ZonedDateTime.now());
        List<Card> cards = new ArrayList<>();
        for (CardStateForm cs : deckStateForm.getCards()) {
            Card card = cardRepository.findById(UUID.fromString(cs.getCardId()))
                    .map(CardMapper::toDomain)
                    .orElseThrow(() -> new RuntimeException("Card not found: " + cs.getCardId()));
            card.setChecked(cs.isChecked());
            card.setQuantity(cs.getQuantity());
            cards.add(card);
        }
        deck.setCards(cards);
        return deck;
    }

    private boolean isValidUUID(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}