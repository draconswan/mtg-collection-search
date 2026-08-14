package com.dswan.mtg.domain.mapper;

import com.dswan.mtg.domain.cards.UserCardDeckAssignment;
import com.dswan.mtg.domain.entity.UserCardDeckAssignmentEntity;

public class UserCardDeckAssignmentMapper {

    public static UserCardDeckAssignment toDomain(UserCardDeckAssignmentEntity e) {
        return new UserCardDeckAssignment(
                e.getId(),
                e.getUserCollectionId(),
                e.getDeckId(),
                e.getAssignedQuantity()
        );
    }

    public static UserCardDeckAssignmentEntity toEntity(UserCardDeckAssignment domain) {
        var e = new UserCardDeckAssignmentEntity();
        e.setId(domain.id());
        e.setUserCollectionId(domain.userCollectionId());
        e.setDeckId(domain.deckId());
        e.setAssignedQuantity(domain.assignedQuantity());
        return e;
    }
}
