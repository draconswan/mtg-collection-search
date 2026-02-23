package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.entity.*;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.domain.mapper.DeckMapper;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.repository.DeckCardRepository;
import com.dswan.mtg.repository.DeckRepository;
import com.dswan.mtg.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dswan.mtg.repository.CardRepository.CARD_WITH_ID_NOT_FOUND;
import static com.dswan.mtg.repository.DeckRepository.DECK_WITH_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeckService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final DeckCardRepository deckCardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Deck saveDeck(Deck deck) {
        DeckEntity entity;
        if (deck.getId() == null) {
            entity = DeckMapper.toNewEntity(deck);
            entity.setUser(currentUser());
            deckRepository.save(entity);
        } else {
            entity = deckRepository.findById(deck.getId())
                    .orElseThrow(() -> new RuntimeException("Deck not found"));
            DeckMapper.updateEntity(entity, deck);
        }
        // Sync cards safely
        DeckMapper.syncCards(entity, deck);
        deckRepository.save(entity);
        return DeckMapper.toDomain(entity);
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public Deck updateDeck(Deck deck) {
        DeckEntity existing = deckRepository.findById(deck.getId())
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        // Update simple fields
        existing.setDeckName(deck.getName());
        existing.setDeckType(deck.getType());

        // Sync cards
        syncCards(existing, deck);
        return DeckMapper.toDomain(existing);
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

    @Transactional
    public Deck getDeck(Long id) {
        Optional<DeckEntity> entity = deckRepository.findById(id);
        if (entity.isEmpty()) {
            log.warn("Deck with id {} not found", id);
            return null;
        }
        return DeckMapper.toDomain(entity.get());
    }

    @Transactional
    public List<Deck> getDecksForUser(Long userId) {
        List<DeckEntity> entities = deckRepository.findByUserId(userId);
        List<Deck> decks = new ArrayList<>();
        for (DeckEntity entity : entities) {
            decks.add(DeckMapper.toDomain(entity));
        }
        return decks;
    }

    @Transactional
    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            log.warn(String.format(DECK_WITH_ID_NOT_FOUND, id));
            return;
        }
        deckRepository.deleteById(id);
    }

    @Transactional
    public boolean removeCardFromDeck(Long deckId, String cardId) {
        try {
            DeckEntity deck = deckRepository.findById(deckId)
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(deckId);
            deckCardId.setCardId(cardId);
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId).orElse(null);
            if (deckCardEntity == null) {
                return false;
            }
            deck.getCards().remove(deckCardEntity);
            deckRepository.save(deck);
        } catch (Exception ex) {
            log.warn("Failed to remove card {} from deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }

    @Transactional
    public boolean addCardToDeck(Long deckId, String cardId) {
        try {
            DeckEntity deck = deckRepository.findById(deckId)
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            CardEntity card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(deckId);
            deckCardId.setCardId(cardId);
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId).orElse(null);
            if (deckCardEntity == null) {
                deckCardEntity = new DeckCardEntity();
                deckCardEntity.setDeckEntity(deck);
                deckCardEntity.setCard(card);
                deckCardEntity.setQuantity(1);
                deckCardEntity.setId(deckCardId);
                deckCardEntity.setChecked(false);
                deckCardRepository.save(deckCardEntity);
            }
        } catch (Exception ex) {
            log.warn("Failed to add card {} to deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }

    @Transactional
    public boolean updateDeckCardQuantity(Long deckId, String cardId, Integer newQuantity) {
        try {
            deckRepository.findById(deckId)
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(deckId);
            deckCardId.setCardId(cardId);
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId)
                    .orElseThrow(() -> new RuntimeException(String.format("Card with id %s not found in deck with id %d", cardId, deckId)));
            deckCardEntity.setQuantity(newQuantity);
            deckCardRepository.save(deckCardEntity);
        } catch (Exception ex) {
            log.warn("Failed to update card quantity for card {} in deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }

    @Transactional
    public boolean updateDeckCardChecked(Long deckId, String cardId, Boolean checked) {
        try {
            deckRepository.findById(deckId)
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(deckId);
            deckCardId.setCardId(cardId);
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId)
                    .orElseThrow(() -> new RuntimeException(String.format("Card with id %s not found in deck with id %d", cardId, deckId)));
            deckCardEntity.setChecked(checked);
            deckCardRepository.save(deckCardEntity);
        } catch (Exception ex) {
            log.warn("Failed to update card checked state for card {} in deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }
}