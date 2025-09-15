package com.dswan.mtg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardEntry {
    private Integer quantity;
    private Card card;
}
