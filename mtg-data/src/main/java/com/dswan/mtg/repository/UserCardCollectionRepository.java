package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.CardEntity;
import com.dswan.mtg.domain.entity.User;
import com.dswan.mtg.domain.entity.UserCardCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCardCollectionRepository extends JpaRepository<UserCardCollectionEntity, UUID> {

    List<UserCardCollectionEntity> findByUserId(Long userId);

    Optional<UserCardCollectionEntity> findByUserAndCard(User user, CardEntity card);

    boolean existsByUserIdAndCardId(Long userId, UUID cardId);
}
