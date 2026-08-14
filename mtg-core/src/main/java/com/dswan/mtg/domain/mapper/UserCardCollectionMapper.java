package com.dswan.mtg.domain.mapper;

import com.dswan.mtg.domain.cards.UserCardCollection;
import com.dswan.mtg.domain.entity.UserCardCollectionEntity;

public class UserCardCollectionMapper {

    public static UserCardCollection toDomain(UserCardCollectionEntity e) {
        return new UserCardCollection(
                e.getId(),
                e.getUser().getId(),
                e.getCard().getId(),
                e.getQuantity(),
                e.getCondition(),
                e.getIsFoil(),
                e.getAcquiredAt(),
                e.getNotes()
        );
    }

    public static UserCardCollectionEntity toEntity(UserCardCollection domain) {
        var e = new UserCardCollectionEntity();
        e.setId(domain.id());
//        e.setUser();
//        e.setCardId(domain.cardId());
        e.setQuantity(domain.quantity());
        e.setCondition(domain.condition());
        e.setIsFoil(domain.isFoil());
        e.setAcquiredAt(domain.acquiredAt());
        e.setNotes(domain.notes());
        return e;
    }
}
