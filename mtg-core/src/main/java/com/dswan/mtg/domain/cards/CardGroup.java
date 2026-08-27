package com.dswan.mtg.domain.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardGroup {
    private String groupName;
    private List<CardEntry> entries;

    public int getTotalQuantity() {
        return entries.stream()
                .mapToInt(CardEntry::getQuantity)
                .sum();
    }

    public int getTotalProxies() {
        return entries.stream()
                .filter(c -> c.getCard().isProxy())
                .mapToInt(CardEntry::getQuantity)
                .sum();
    }
}
