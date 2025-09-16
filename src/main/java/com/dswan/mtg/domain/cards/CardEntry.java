package com.dswan.mtg.domain.cards;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardEntry {
    private Integer quantity;
    private Card card;
}
