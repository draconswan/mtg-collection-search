package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.domain.entity.UserLandGroupReportDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandGroupReportRepository extends JpaRepository<DeckEntity, Long> {

    @Query(
            value = """
                    SELECT
                        lg.name AS cardName,
                        lg.group_name AS landGroup,
                        CAST(SUM(udc.quantity) AS BIGINT) AS totalCount
                    FROM user_decks ud
                    JOIN user_deck_cards udc
                        ON ud.id = udc.deck_id
                    JOIN land_cycles_view lg
                        ON lg.id = udc.card_id
                    WHERE ud.user_id = :userId
                    GROUP BY lg.name, lg.group_name
                    ORDER BY lg.group_name, lg.name
                    """,
            nativeQuery = true
    )
    List<UserLandGroupReportDto> getUserLandGroupReport(@Param("userId") Long userId);
}