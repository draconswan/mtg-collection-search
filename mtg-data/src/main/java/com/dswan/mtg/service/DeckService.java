package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.entity.*;
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
    public Deck createDeck(Deck deck) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        DeckEntity deckEntity = DeckMapper.toEntity(deck);
        deckEntity.setUser(user);
        List<DeckCardEntity> pendingCards = new ArrayList<>(deckEntity.getCards());
        deckEntity.setCards(new ArrayList<>());
        deckEntity = deckRepository.save(deckEntity);
        Long deckId = deckEntity.getId();

        List<DeckCardEntity> finalCards = new ArrayList<>();
        for (DeckCardEntity dce : pendingCards) {
            DeckCardEntity newDce = new DeckCardEntity();
            newDce.setDeckEntity(deckEntity);
            newDce.setCard(dce.getCard());
            DeckCardId id = new DeckCardId();
            id.setDeckId(deckId);
            id.setCardId(dce.getCard().getId());
            newDce.setId(id);
            newDce.setChecked(dce.getChecked());
            newDce.setQuantity(dce.getQuantity());
            finalCards.add(newDce);
        }
        deckEntity.setCards(finalCards);
        deckEntity = deckRepository.save(deckEntity);
        return DeckMapper.toDomain(deckEntity);
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