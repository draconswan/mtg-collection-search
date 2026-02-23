package com.dswan.mtg.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class DeckStateForm {
    private Long deckId;
    private String deckName;
    private String deckFormat;
    private List<CardStateForm> cards;
}