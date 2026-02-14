package com.dswan.mtg.domain.cards;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class Deck {
    private Long id;
    private String name;
    private String type;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastUpdated;
    private List<Card> cards;
}
