package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.UserCardDeckAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCardDeckAssignmentRepository extends JpaRepository<UserCardDeckAssignmentEntity, UUID> {

    List<UserCardDeckAssignmentEntity> findByDeckId(UUID deckId);

    List<UserCardDeckAssignmentEntity> findByUserCollectionId(UUID userCollectionId);

    List<UserCardDeckAssignmentEntity> findByDeckIdAndUserCollectionId(UUID deckId, UUID userCollectionId);
}
