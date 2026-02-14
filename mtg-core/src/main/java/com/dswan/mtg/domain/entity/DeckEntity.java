package com.dswan.mtg.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "user_decks")
public class DeckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deckname", nullable = false)
    private String deckName;

    @Column(name = "decktype", nullable = false)
    private String deckType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @OneToMany(mappedBy = "deckEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckCardEntity> cards = new ArrayList<>();
}