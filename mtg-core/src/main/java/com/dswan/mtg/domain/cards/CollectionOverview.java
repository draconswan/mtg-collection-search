package com.dswan.mtg.domain.cards;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record CollectionOverview(Long totalCards, Long numMythics, Long numRares, Long numUncommons, Long numCommons, Map<String, Long> cardsByColor, Long numProxies, Long numSets,
                                 BigDecimal approximateValue, LocalDateTime lastRefresh) {
}
