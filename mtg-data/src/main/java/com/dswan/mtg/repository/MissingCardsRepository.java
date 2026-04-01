package com.dswan.mtg.repository;

import com.dswan.mtg.dto.UncheckedCardDTO;
import com.dswan.mtg.domain.entity.DeckCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissingCardsRepository extends JpaRepository<DeckCardEntity, Long> {

    @Query(value = """
                WITH missing_per_deck AS (
                    SELECT
                        ud.id AS deck_id,
                        ud.deckname AS deck_name,
                        c.oracle_id,
                        SUM(udc.quantity)::bigint AS missing_quantity
                    FROM user_deck_cards udc
                    JOIN card c ON c.id = udc.card_id
                    JOIN user_decks ud ON ud.id = udc.deck_id
                    WHERE ud.user_id = :userId
                      AND c.oracle_id NOT IN (
                          SELECT DISTINCT c2.oracle_id
                          FROM user_deck_cards udc2
                          JOIN card c2 ON c2.id = udc2.card_id
                          JOIN user_decks ud2 ON ud2.id = udc2.deck_id
                          WHERE ud2.user_id = :userId
                            AND udc2.checked = true
                      )
                      AND (:types IS NULL OR LOWER(ud.decktype) = ANY(CAST(:types AS text[])))
                      AND c.type_line NOT ILIKE '%basic land%'
                    GROUP BY ud.id, ud.deckname, c.oracle_id
                ),
                all_printings AS (
                    SELECT DISTINCT ON (c.oracle_id, c.set_code)
                        c.id AS card_id,
                        c.oracle_id,
                        c.name AS card_name,
                        c.printed_name,
                        c.flavor_name,
                        c.lang,
                        c.set_code,
                        c.set_name,
                        c.released_at,
                        c.type_line,
                        c.mana_cost
                    FROM card c
                    WHERE c.type_line NOT ILIKE '%basic land%'
                    ORDER BY c.oracle_id, c.set_code, c.released_at DESC
                )
                SELECT
                    mpd.deck_id,
                    mpd.deck_name,
                    ap.card_id,
                    ap.oracle_id,
                    ap.card_name,
                    ap.printed_name,
                    ap.flavor_name,
                    ap.lang,
                    ap.set_code,
                    ap.set_name,
                    ap.released_at,
                    ap.type_line,
                    ap.mana_cost,
                    mpd.missing_quantity AS quantity
                FROM missing_per_deck mpd
                JOIN all_printings ap ON ap.oracle_id = mpd.oracle_id
                ORDER BY ap.released_at DESC, ap.card_name, mpd.deck_name;
            """,
            nativeQuery = true
    )
    List<UncheckedCardDTO> findAllUncheckedCardsForUser(@Param("userId") Long userId,
                                                        @Param("types") String[] types);
}