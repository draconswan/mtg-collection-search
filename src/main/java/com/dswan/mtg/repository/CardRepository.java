package com.dswan.mtg.repository;

import com.dswan.mtg.domain.cards.Card;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    @Query(value = """
            SELECT *
            FROM card c
            WHERE lower(c.name) = lower(:card_name)
              OR split_part(lower(c.name), ' // ', 1) = lower(:card_name)
              OR split_part(lower(c.name), ' // ', 2) = lower(:card_name)
              OR lower(c.flavor_name) = lower(:card_name)
              OR split_part(lower(c.flavor_name), ' // ', 1) = lower(:card_name)
              OR split_part(lower(c.flavor_name), ' // ', 2) = lower(:card_name)
            ORDER BY c.set_code
            """, nativeQuery = true)
    List<Card> findAllPrintingsForCardName(
            @Param("card_name") String card_name
    );
}
