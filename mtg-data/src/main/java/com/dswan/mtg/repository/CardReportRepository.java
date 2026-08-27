package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.domain.entity.UserLandGroupReportDto;
import com.dswan.mtg.domain.entity.UserProxyReportDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardReportRepository extends JpaRepository<DeckEntity, Long> {

    @Query(value = """
            SELECT
                lg.name AS cardName,
                lg.group_name AS landGroup,
                CAST(SUM(udc.quantity) AS BIGINT) AS totalCount,
                CAST(SUM(CASE WHEN udc.checked = TRUE THEN udc.quantity ELSE 0 END) AS BIGINT) AS checkedCount,
                CAST(SUM(CASE WHEN udc.checked IS NOT TRUE THEN udc.quantity ELSE 0 END) AS BIGINT) AS uncheckedCount
            FROM user_decks ud
            JOIN user_deck_cards udc
                ON ud.id = udc.deck_id
            JOIN land_cycles_view lg
                ON lg.id = udc.card_id
            WHERE ud.user_id = :userId
            GROUP BY lg.name, lg.group_name
            ORDER BY lg.group_name, lg.name;
            """,
            nativeQuery = true
    )
    List<UserLandGroupReportDto> getUserLandGroupReport(@Param("userId") Long userId);

    @Query(value = """
            WITH oracle_ids AS (
                SELECT DISTINCT c.oracle_id
                FROM user_decks ud
                JOIN user_deck_cards udc ON ud.id = udc.deck_id
                JOIN card c ON c.id = udc.card_id
                WHERE ud.user_id = :userId
                  AND udc.proxy = true
            ),
            earliest_cards AS (
                SELECT DISTINCT ON (oracle_id)
                    oracle_id,
                    name,
                    released_at
                FROM card
                WHERE oracle_id IN (SELECT oracle_id FROM oracle_ids)
                ORDER BY oracle_id, released_at
            )
            SELECT
                ec.name AS card_name,
                CAST(SUM(udc.quantity) AS BIGINT) AS total_count
            FROM user_decks ud
            JOIN user_deck_cards udc ON ud.id = udc.deck_id
            JOIN card c ON c.id = udc.card_id
            JOIN earliest_cards ec ON ec.oracle_id = c.oracle_id
            WHERE ud.user_id = :userId
              AND udc.proxy = true
            GROUP BY ec.oracle_id, ec.name, ec.released_at
            ORDER BY ec.name;
            """,
            nativeQuery = true
    )
    List<UserProxyReportDto> getUserProxiesReport(@Param("userId") Long userId);
}