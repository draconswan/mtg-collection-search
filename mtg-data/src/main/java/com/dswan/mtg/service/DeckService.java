package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.cards.DeckZone;
import com.dswan.mtg.domain.entity.*;
import com.dswan.mtg.domain.mapper.DeckMapper;
import com.dswan.mtg.dto.DeckSummaryView;
import com.dswan.mtg.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final LandGroupReportRepository landGroupReportRepository;

    @Transactional
    public Deck saveDeck(Deck deck) {
        DeckEntity entity;
        if (deck.getId() == null) {
            entity = DeckMapper.toNewEntity(deck);
            entity.setUser(currentUser());
            deckRepository.save(entity);
        } else {
            entity = deckRepository.findById(UUID.fromString(deck.getId()))
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
    public Deck getDeck(String deckId) {
        Optional<DeckEntity> entity = deckRepository.findById(UUID.fromString(deckId));
        if (entity.isEmpty()) {
            log.warn("Deck with id {} not found", deckId);
            return null;
        }
        return DeckMapper.toDomain(entity.get());
    }

    @Transactional
    public List<DeckSummaryView> getDecksSummaryForUser(Long userId) {
        return deckRepository.findDeckSummaries(userId);
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
    public void deleteDeck(String id) {
        if (!deckRepository.existsById(UUID.fromString(id))) {
            log.warn(String.format(DECK_WITH_ID_NOT_FOUND, id));
            return;
        }
        deckRepository.deleteById(UUID.fromString(id));
    }

    @Transactional
    public boolean removeCardFromDeck(String deckId, String cardId, String zone) {
        try {
            DeckEntity deck = deckRepository.findById(UUID.fromString(deckId))
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(UUID.fromString(cardId))
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(UUID.fromString(deckId));
            deckCardId.setCardId(UUID.fromString(cardId));
            deckCardId.setLocation(DeckZone.fromString(zone).name().toLowerCase());
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
    public boolean addCardToDeck(String deckId, String cardId, String zone) {
        try {
            DeckEntity deck = deckRepository.findById(UUID.fromString(deckId))
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            CardEntity card = cardRepository.findById(UUID.fromString(cardId))
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(UUID.fromString(deckId));
            deckCardId.setCardId(UUID.fromString(cardId));
            deckCardId.setLocation(DeckZone.fromNullableString(zone).name().toLowerCase());
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
    public boolean updateDeckCardQuantity(String deckId, String cardId, Integer newQuantity, String zone) {
        try {
            deckRepository.findById(UUID.fromString(deckId))
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(UUID.fromString(cardId))
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(UUID.fromString(deckId));
            deckCardId.setCardId(UUID.fromString(cardId));
            deckCardId.setLocation(DeckZone.fromString(zone).name().toLowerCase());
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId)
                    .orElseThrow(() -> new RuntimeException(String.format("Card with id %s not found in deck with id %s", cardId, deckId)));
            deckCardEntity.setQuantity(newQuantity);
            deckCardRepository.save(deckCardEntity);
        } catch (Exception ex) {
            log.warn("Failed to update card quantity for card {} in deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }

    @Transactional
    public boolean updateDeckCardChecked(UUID deckId, UUID cardId, String zone, Boolean checked) {
        try {
            deckRepository.findById(deckId)
                    .orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckCardId deckCardId = new DeckCardId();
            deckCardId.setDeckId(deckId);
            deckCardId.setCardId(cardId);
            deckCardId.setLocation(DeckZone.fromString(zone).name().toLowerCase());
            DeckCardEntity deckCardEntity = deckCardRepository.findById(deckCardId)
                    .orElseThrow(() -> new RuntimeException(String.format("Card with id %s not found in deck with id %s", cardId, deckId)));
            deckCardEntity.setChecked(checked);
            deckCardRepository.save(deckCardEntity);
        } catch (Exception ex) {
            log.warn("Failed to update card checked state for card {} in deck {}", cardId, deckId, ex);
            return false;
        }
        return true;
    }

    @Transactional
    public boolean moveCard(String deckId, String cardId, int quantity, String currentZoneRaw, String targetZoneRaw) {
        try {
            UUID deckUUID = UUID.fromString(deckId);
            UUID cardUUID = UUID.fromString(cardId);
            DeckEntity deck = deckRepository.findById(deckUUID).orElseThrow(() -> new RuntimeException(String.format(DECK_WITH_ID_NOT_FOUND, deckId)));
            CardEntity card = cardRepository.findById(cardUUID).orElseThrow(() -> new RuntimeException(String.format(CARD_WITH_ID_NOT_FOUND, cardId)));
            DeckZone targetZone = DeckZone.fromString(targetZoneRaw);
            DeckZone currentZone = DeckZone.fromString(currentZoneRaw);
            DeckCardId currentId = new DeckCardId();
            currentId.setDeckId(deckUUID);
            currentId.setCardId(cardUUID);
            currentId.setLocation(currentZone.name().toLowerCase());
            DeckCardEntity source = deckCardRepository.findById(currentId).orElseThrow(() -> new RuntimeException(String.format("Card %s not found in deck %s", cardId, deckId)));
            int currentQty = source.getQuantity();
            if (quantity < 1 || quantity > currentQty) {
                throw new IllegalArgumentException("Invalid quantity to move");
            }
            // Subtract from original
            source.setQuantity(currentQty - quantity);
            // Find existing entry in target zone
            DeckCardId targetId = new DeckCardId();
            targetId.setDeckId(deckUUID);
            targetId.setCardId(cardUUID);
            targetId.setLocation(targetZone.name().toLowerCase());
            DeckCardEntity existingTarget = deckCardRepository.findById(targetId).orElse(null);
            if (existingTarget != null) {
                // Merge quantities
                existingTarget.setQuantity(existingTarget.getQuantity() + quantity);
                deckCardRepository.save(existingTarget);
            } else {
                // Create new DeckCardEntity in target zone
                DeckCardEntity newEntry = new DeckCardEntity();
                DeckCardId newId = new DeckCardId();
                newId.setDeckId(deckUUID);
                newId.setCardId(cardUUID);
                newId.setLocation(targetZone.name().toLowerCase());
                newEntry.setId(newId);
                newEntry.setDeckEntity(deck);
                newEntry.setCard(card);
                newEntry.setQuantity(quantity);
                newEntry.setChecked(source.getChecked());
                newEntry.setProxy(source.getProxy());
                deckCardRepository.save(newEntry);
            }
            // Remove original if empty
            if (source.getQuantity() == 0) {
                deckCardRepository.delete(source);
            } else {
                deckCardRepository.save(source);
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to move card {} in deck {}: {}", cardId, deckId, ex.getMessage());
            return false;
        }
    }

    public List<UserLandGroupReportDto> getLandAuditForUser(Long userId) {
        return landGroupReportRepository.getUserLandGroupReport(userId);
    }
}