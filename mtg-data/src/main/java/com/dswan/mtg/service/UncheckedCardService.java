package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.mapper.DeckMapper;
import com.dswan.mtg.dto.UncheckedCardDTO;
import com.dswan.mtg.dto.UncheckedCardView;
import com.dswan.mtg.repository.DeckRepository;
import com.dswan.mtg.repository.MissingCardsRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UncheckedCardService {

    private final MissingCardsRepository missingCardsRepository;
    private final DeckRepository deckRepository;

    public Map<String, List<UncheckedCardView>> getUncheckedCardsGroupedBySet(Long userId,
                                                                              List<String> types,
                                                                              List<UUID> deckIds,
                                                                              int page,
                                                                              int size,
                                                                              AtomicReference<Integer> totalPagesRef) {
        // 1. Load all rows (your existing logic)
        Map<String, List<UncheckedCardView>> grouped = getUncheckedCardsGroupedBySet(userId, types, deckIds);

        // 2. Extract ordered set codes
        List<String> setCodes = new ArrayList<>(grouped.keySet());

        // 3. Compute total pages
        int totalSets = setCodes.size();
        int totalPages = (int) Math.ceil((double) totalSets / size);
        totalPagesRef.set(totalPages);

        // 4. Slice the sets for this page
        int from = page * size;
        int to = Math.min(from + size, totalSets);

        return setCodes.subList(from, to).stream()
                .collect(Collectors.toMap(
                        code -> code,
                        grouped::get,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    public Map<String, List<UncheckedCardView>> getUncheckedCardsGroupedBySet(Long userId, List<String> types, List<UUID> deckIds) {
        if (types == null || types.isEmpty()) {
            types = DeckFormats.FORMATS.values()
                    .stream()
                    .flatMap(List::stream)
                    .map(t -> t.toLowerCase().replace(" ", "_"))
                    .toList();
        } else {
            types = types.stream()
                    .map(t -> t.toLowerCase().replace(" ", "_"))
                    .toList();
        }
        String[] typesArray = types.toArray(new String[0]);
        UUID[] decksArray = !CollectionUtils.isEmpty(deckIds) ? deckIds.toArray(new UUID[0]) : null;
        List<UncheckedCardDTO> rows = missingCardsRepository.findAllUncheckedCardsForUser(userId, typesArray, decksArray);
        rows.forEach(UncheckedCardDTO::hydrateFromEntity);
        Map<UUID, List<String>> deckColorsCache = new HashMap<>();
        List<UncheckedCardView> wrapped = rows.stream()
                .map(dto -> {
                    List<String> colors = deckColorsCache.computeIfAbsent(
                            dto.getDeckId(),
                            id -> {
                                var deckEntity = deckRepository.findById(id).orElseThrow();
                                var deck = DeckMapper.toDomain(deckEntity);
                                deck.calculateDeckColors();
                                return deck.getDeckColors();
                            }
                    );
                    return new UncheckedCardView(dto, colors);
                })
                .toList();
        return wrapped.stream()
                .collect(Collectors.groupingBy(
                        view -> view.base().getSetCode(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}