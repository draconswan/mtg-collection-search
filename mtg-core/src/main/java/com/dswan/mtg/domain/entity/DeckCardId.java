package com.dswan.mtg.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class DeckCardId implements Serializable {
    private Long deckId;
    private String cardId;
}