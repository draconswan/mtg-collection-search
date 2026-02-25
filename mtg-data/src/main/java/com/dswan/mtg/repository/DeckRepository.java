package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<DeckEntity, UUID> {
    String DECK_WITH_ID_NOT_FOUND = "Deck with id %s not found";

    List<DeckEntity> findByUserId(Long userId);
}