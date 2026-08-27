package com.dswan.mtg.domain.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckZoneGroup {
    private DeckZone zone;
    private List<CardGroup> groups;

    public int getTotalQuantity() {
        return groups.stream()
                .mapToInt(CardGroup::getTotalQuantity)
                .sum();
    }

    public int getTotalProxies() {
        return groups.stream()
                .mapToInt(CardGroup::getTotalProxies)
                .sum();
    }
}

