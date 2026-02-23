package com.dswan.mtg.util;

import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.CardType;

import java.util.*;

public class DeckProcessingUtil {
    public static final List<CardType> TYPE_ORDER = List.of(
            CardType.PLANESWALKER, CardType.CREATURE, CardType.INSTANT, CardType.SORCERY,
            CardType.ARTIFACT, CardType.ENCHANTMENT, CardType.BATTLE, CardType.LAND
    );

    public static Map<String, List<CardEntry>> getOrderedCardGroups(List<CardEntry> cardEntries, Map<String, Integer> typeQuantities) {
        Map<String, List<CardEntry>> groupedDecklist = new LinkedHashMap<>();
        for (CardEntry entry : cardEntries) {
            String type = entry.getCard()
                    .getCardTypes()
                    .getCardType()
                    .getLast()
                    .toString();
            groupedDecklist.computeIfAbsent(type, _ -> new ArrayList<>()).add(entry);
            typeQuantities.merge(type, entry.getQuantity(), Integer::sum);
        }
        groupedDecklist.values().forEach(group -> group.sort(Comparator.comparing(e -> e.getCard().getDisplayName().toLowerCase())));
        Map<String, List<CardEntry>> orderedGroups = new LinkedHashMap<>();
        for (CardType ct : TYPE_ORDER) {
            String key = ct.toString(); // or ct.name(), depending on your enum
            if (groupedDecklist.containsKey(key)) {
                orderedGroups.put(key, groupedDecklist.get(key));
            }
        }
        // Add any unexpected types at the end
        groupedDecklist.keySet().stream()
                .filter(k -> !orderedGroups.containsKey(k))
                .forEach(k -> orderedGroups.put(k, groupedDecklist.get(k)));
        return orderedGroups;
    }
}
