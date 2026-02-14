package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.DeckCardEntity;
import com.dswan.mtg.domain.entity.DeckCardId;
import com.dswan.mtg.domain.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckCardRepository extends JpaRepository<DeckCardEntity, Long> {

    Optional<DeckCardEntity> findById(DeckCardId id);
}