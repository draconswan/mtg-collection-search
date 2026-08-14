package com.dswan.mtg.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_card_collection",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}))
public class UserCardCollectionEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @Column(nullable = false)
    private int quantity;

    private String condition;

    @Column(name = "is_foil")
    private Boolean isFoil;

    @Column(name = "acquired_at")
    private OffsetDateTime acquiredAt;

    private String notes;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
