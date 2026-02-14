package com.dswan.mtg.domain.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardFace {
    private String oracleId;
    private String name;
    private String typeLine;
    private String manaCost;
    private Images imageUris;
}
