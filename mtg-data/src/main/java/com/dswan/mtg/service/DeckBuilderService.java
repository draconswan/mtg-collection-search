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

@Service
@RequiredArgsConstructor
public class DeckBuilderService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    public Deck buildDeck(DeckStateForm deckStateForm) {
        Deck deck;
        if (deckStateForm.getDeckId() != null) {
            Optional<DeckEntity> existingDeck = deckRepository.findById(deckStateForm.getDeckId());
            if (existingDeck.isPresent()) {
                deck = DeckMapper.toDomain(existingDeck.get());
            } else {
                deck = new Deck();
                deck.setName("New Deck");
                deck.setType("Unknown");
                deck.setCreatedAt(ZonedDateTime.now());
            }
        } else {
            deck = new Deck();
            deck.setName("New Deck");
            deck.setType("Unknown");
            deck.setCreatedAt(ZonedDateTime.now());
        }
        deck.setLastUpdated(ZonedDateTime.now());
        List<Card> cards = new ArrayList<>();
        for (CardStateForm cs : deckStateForm.getCards()) {
            Card card = cardRepository.findById(cs.getCardId())
                    .map(CardMapper::toDomain)
                    .orElseThrow(() -> new RuntimeException("Card not found: " + cs.getCardId()));
            card.setChecked(cs.isChecked());
            card.setQuantity(cs.getQuantity());
            cards.add(card);
        }
        deck.setCards(cards);
        return deck;
    }
}