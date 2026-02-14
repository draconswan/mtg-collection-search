package com.dswan.mtg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class DeckStateForm {
    private Long deckId;
    private List<CardStateForm> cards;
}