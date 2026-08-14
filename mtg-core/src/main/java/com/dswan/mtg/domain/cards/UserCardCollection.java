package com.dswan.mtg.domain.cards;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserCardCollection(
        UUID id,
        Long userId,
        UUID cardId,
        int quantity,
        String condition,
        Boolean isFoil,
        OffsetDateTime acquiredAt,
        String notes
) {}
