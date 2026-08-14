package com.dswan.mtg.domain.cards;

import java.util.UUID;

public record UserCardDeckAssignment(
        UUID id,
        UUID userCollectionId,
        UUID deckId,
        int assignedQuantity
) {}
