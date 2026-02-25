package com.dswan.mtg.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class DeckCardId implements Serializable {
    private UUID deckId;
    private UUID cardId;
}