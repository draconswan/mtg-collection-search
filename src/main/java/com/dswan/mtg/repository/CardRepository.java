package com.dswan.mtg.repository;

import com.dswan.mtg.domain.Card;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    @Query(value = """
            SELECT
                c.id,
                c.name,
                s.code,
                s.name,
                cs.rarity,
                cs.collector_number
            FROM card c
            WHERE c.name = :card_name
            JOIN card_set cs
                ON card.id = cs.card_id
            JOIN set s
                on cs.set_code = s.code
            ORDER BY s.name
            """, nativeQuery = true)
    List<Card> findAllPrintingsForCardName(
            @Param("card_name") String cardName
    );
}
