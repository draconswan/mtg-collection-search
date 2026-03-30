package com.dswan.mtg.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class DeckCardCheckState {
    private UUID deckId;
    private UUID cardId;
    private boolean checked;
}
