package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.CollectionOverview;
import com.dswan.mtg.domain.cards.UserCardCollection;
import com.dswan.mtg.domain.cards.UserCardDeckAssignment;
import com.dswan.mtg.domain.entity.*;
import com.dswan.mtg.domain.mapper.UserCardCollectionMapper;
import com.dswan.mtg.domain.mapper.UserCardDeckAssignmentMapper;
import com.dswan.mtg.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserCollectionService {

    private final CardRepository cardRepository;
    private final DeckCardRepository deckCardRepository;
    private final UserCardCollectionRepository collectionRepo;
    private final UserCardDeckAssignmentRepository assignmentRepo;
    private final DataVersionRepository dataVersionRepository;

    public UserCollectionService(CardRepository cardRepository,
                                 DeckCardRepository deckCardRepository,
                                 UserCardCollectionRepository collectionRepo,
                                 UserCardDeckAssignmentRepository assignmentRepo,
                                 DataVersionRepository dataVersionRepository) {
        this.cardRepository = cardRepository;
        this.deckCardRepository = deckCardRepository;
        this.collectionRepo = collectionRepo;
        this.assignmentRepo = assignmentRepo;
        this.dataVersionRepository = dataVersionRepository;
    }

    public CollectionOverview getUserCollectionOverview(Long userId) {
        // 1. Load all collection entries for the user
        var collection = collectionRepo.findByUserId(userId);

        if (collection.isEmpty()) {
            return new CollectionOverview(
                    0L, 0L, 0L, 0L, 0L,
                    Map.of(), 0L, 0L,
                    BigDecimal.ZERO,
                    LocalDateTime.now()
            );
        }

        // 2. Total cards owned
        long totalCards = collection.stream()
                .mapToLong(UserCardCollectionEntity::getQuantity)
                .sum();

        // 3. Rarity counts
        long numMythics = 0, numRares = 0, numUncommons = 0, numCommons = 0;

        for (var col : collection) {
            var card = col.getCard();
            if (card == null) {
                continue;
            }

            switch (card.getRarity().toLowerCase()) {
                case "mythic" -> numMythics += col.getQuantity();
                case "rare" -> numRares += col.getQuantity();
                case "uncommon" -> numUncommons += col.getQuantity();
                case "common" -> numCommons += col.getQuantity();
            }
        }

        // 4. Cards by color
        Map<String, Long> cardsByColor = new HashMap<>();
        for (var col : collection) {
            var card = col.getCard();
            if (card == null) {
                continue;
            }

            var color = card.getColor(); // e.g., "W", "U", "B", "R", "G", "C", "WU", etc.

            if (color != null && !color.isBlank()) {
                String formattedColor = Arrays.stream(color.split("")).map(c -> "{" + c + "}").collect(Collectors.joining());
                cardsByColor.merge(formattedColor, (long) col.getQuantity(), Long::sum);
            } else {
                cardsByColor.merge("{C}", (long) col.getQuantity(), Long::sum);
            }
        }

        Map<String, Long> sortedCardsByColor = cardsByColor.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        // 5. Number of sets represented
        long numSets = collection.stream()
                .map(col -> col.getCard().getSetCode())
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // 6. Proxy count (deck-only)
        long numProxies = deckCardRepository.countProxiesByUser(userId);

        // 7. Approximate value (sum of price * quantity)
        BigDecimal approximateValue = BigDecimal.ZERO;

        for (UserCardCollectionEntity col : collection) {
            CardEntity card = col.getCard();
            if (card == null) {
                continue;
            }

            BigDecimal price = extractPrice(card.getPricesList());
            if (price != null) {
                approximateValue = approximateValue.add(price.multiply(BigDecimal.valueOf(col.getQuantity())));
            }
        }

        // 8. Last refresh from data_version
        List<DataVersionEntity> dataVersion = (List<DataVersionEntity>) dataVersionRepository.findAll();
        LocalDateTime lastRefresh;
        if (CollectionUtils.isEmpty(dataVersion)) {
            lastRefresh = LocalDateTime.now();
        } else {
            lastRefresh = dataVersion.getFirst().getLastRefresh();
        }

        return new CollectionOverview(
                totalCards,
                numMythics,
                numRares,
                numUncommons,
                numCommons,
                sortedCardsByColor,
                numProxies,
                numSets,
                approximateValue,
                lastRefresh
        );
    }

    @Transactional
    public UserCardCollection addOrUpdateCollection(User user, CardEntity card, int quantity) {
        var existing = collectionRepo.findByUserAndCard(user, card);

        UserCardCollectionEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setQuantity(entity.getQuantity() + quantity);
        } else {
            entity = new UserCardCollectionEntity();
            entity.setUser(user);
            entity.setCard(card);
            entity.setQuantity(quantity);
        }

        return UserCardCollectionMapper.toDomain(collectionRepo.save(entity));
    }

    @Transactional
    public UserCardDeckAssignment assignToDeck(UUID collectionId, UUID deckId, int quantity) {
        var col = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection entry not found"));

        int assigned = assignmentRepo.findByUserCollectionId(collectionId)
                .stream()
                .mapToInt(UserCardDeckAssignmentEntity::getAssignedQuantity)
                .sum();

        if (assigned + quantity > col.getQuantity()) {
            throw new IllegalStateException("Cannot assign more copies than owned");
        }

        var assignment = new UserCardDeckAssignmentEntity();
        assignment.setUserCollectionId(collectionId);
        assignment.setDeckId(deckId);
        assignment.setAssignedQuantity(quantity);

        return UserCardDeckAssignmentMapper.toDomain(assignmentRepo.save(assignment));
    }

    private BigDecimal extractPrice(String pricesJson) {
        if (pricesJson == null || pricesJson.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            var mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(pricesJson);
            String[] fields = {"usd", "usd_foil", "usd_etched"};
            for (String field : fields) {
                JsonNode value = node.get(field);
                if (value != null && !value.isNull()) {
                    try {
                        return new BigDecimal(value.asString());
                    } catch (NumberFormatException _) {
                        // malformed number → skip
                    }
                }
            }
        } catch (Exception _) {
            // malformed JSON → treat as zero
        }
        return BigDecimal.ZERO;
    }
}
