package com.dswan.mtg.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "user_card_deck_assignments")
public class UserCardDeckAssignmentEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_collection_id", nullable = false, columnDefinition = "UUID")
    private UUID userCollectionId;

    @Column(name = "deck_id", nullable = false, columnDefinition = "UUID")
    private UUID deckId;

    @Column(name = "assigned_quantity", nullable = false)
    private int assignedQuantity;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
