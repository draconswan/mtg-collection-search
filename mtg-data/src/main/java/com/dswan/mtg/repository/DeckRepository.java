package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.dto.DeckSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<DeckEntity, UUID> {
    String DECK_WITH_ID_NOT_FOUND = "Deck with id %s not found";

    List<DeckEntity> findByUserId(Long userId);

    @Query("""
    SELECT new com.dswan.mtg.dto.DeckSummaryView(
        d.id,
        d.deckName,
        d.deckType,
        SUM(dc.quantity),
        SUM(CASE WHEN dc.checked = true THEN dc.quantity ELSE 0 END),
        SUM(CASE WHEN dc.proxy = true THEN dc.quantity ELSE 0 END),
        d.colors,
        null
    )
    FROM DeckEntity d
    LEFT JOIN d.cards dc
    WHERE d.user.id = :userId
    GROUP BY d.id
""")
    List<DeckSummaryView> findDeckSummaries(Long userId);

}