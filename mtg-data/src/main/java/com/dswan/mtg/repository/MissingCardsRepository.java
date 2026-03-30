package com.dswan.mtg.repository;

import com.dswan.mtg.dto.UncheckedCardDTO;
import com.dswan.mtg.domain.entity.DeckCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissingCardsRepository extends JpaRepository<DeckCardEntity, Long> {

    @Query(
            value = """
                    SELECT
                          ud.id AS deck_id,
                          ud.deckname AS deck_name,
                          c.id as card_id,
                          c.oracle_id AS oracle_id,
                          c.name AS card_name,
                          c.printed_name AS printed_name,
                          c.flavor_name AS flavor_name,
                          c.lang AS lang,
                          c.set_code AS set_code,
                          c.set_name AS set_name,
                          c.released_at AS released_at,
                          c.type_line AS type_line,
                          c.mana_cost AS mana_cost,
                          udc.quantity AS quantity
                      FROM user_deck_cards udc
                      JOIN card c ON c.id = udc.card_id
                      JOIN user_decks ud ON ud.id = udc.deck_id
                      WHERE c.oracle_id NOT IN (
                          SELECT DISTINCT c2.oracle_id
                          FROM user_deck_cards udc2
                          JOIN card c2 ON c2.id = udc2.card_id
                          JOIN user_decks ud2 ON ud2.id = udc2.deck_id
                          WHERE ud2.user_id = :userId
                            AND udc2.checked = true
                      )
                      AND ud.user_id = :userId
                      AND (:types IS NULL OR LOWER(ud.decktype) = ANY(CAST(:types AS text[])))
                      ORDER BY c.released_at DESC, c.name;
                    """,
            nativeQuery = true
    )
    List<UncheckedCardDTO> findAllUncheckedCardsForUser(@Param("userId") Long userId,
                                                        @Param("types") String[] types);
}