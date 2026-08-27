package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckCardEntity;
import com.dswan.mtg.domain.entity.DeckCardId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckCardRepository extends JpaRepository<DeckCardEntity, DeckCardId> {

    @Query("""
                SELECT dc
                FROM DeckCardEntity dc
                JOIN DeckEntity d ON dc.id.deckId = d.id
                WHERE d.user.id = :userId
                  AND dc.proxy = TRUE
            """)
    List<DeckCardEntity> getProxiesByUser(@Param("userId") Long userId);

    @Query("""
                SELECT COUNT(dc)
                FROM DeckCardEntity dc
                JOIN DeckEntity d ON dc.id.deckId = d.id
                WHERE d.user.id = :userId
                  AND dc.proxy = TRUE
            """)
    long countProxiesByUser(@Param("userId") Long userId);

    @Query("""
                SELECT dc
                FROM DeckCardEntity dc
                JOIN FETCH dc.card c
                JOIN FETCH dc.deckEntity d
                WHERE dc.id.deckId = :deckId
                  AND c.oracleId = :oracleId
            """)
    List<DeckCardEntity> findAllByDeckIdAndOracleId(UUID deckId, String oracleId);

    @Modifying
    @Query("""
                DELETE FROM DeckCardEntity dc
                WHERE dc.id.deckId = :deckId
                  AND dc.id.cardId = :cardId
                  AND dc.id.location = :location
            """)
    void deleteByPrimaryKey(UUID deckId, UUID cardId, String location);
}