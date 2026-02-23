package com.dswan.mtg.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_deck_cards")
public class DeckCardEntity {

    @EmbeddedId
    private DeckCardId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deckId")
    @JoinColumn(name = "deck_id")
    private DeckEntity deckEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cardId")
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Boolean checked = false;

    @Column(nullable = false)
    private String location;
}