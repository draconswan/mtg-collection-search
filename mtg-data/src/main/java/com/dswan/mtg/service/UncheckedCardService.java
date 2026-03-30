package com.dswan.mtg.service;

import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.domain.mapper.DeckMapper;
import com.dswan.mtg.dto.UncheckedCardDTO;
import com.dswan.mtg.dto.UncheckedCardView;
import com.dswan.mtg.repository.DeckRepository;
import com.dswan.mtg.repository.MissingCardsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UncheckedCardService {

    private final MissingCardsRepository missingCardsRepository;
    private final DeckRepository deckRepository;

    public Map<String, List<UncheckedCardView>> getUncheckedCardsGroupedBySet(Long userId, List<String> types) {
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
        List<UncheckedCardDTO> rows =
                missingCardsRepository.findAllUncheckedCardsForUser(userId, types.toArray(new String[0]));
        // Cache deck colors so we compute them only once per deck
        Map<UUID, List<String>> deckColorsCache = new HashMap<>();

        List<UncheckedCardView> wrapped = rows.stream()
                .map(dto -> {
                    List<String> colors = deckColorsCache.computeIfAbsent(
                            dto.deckId(),
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

        // Group by setCode for UI
        return wrapped.stream()
                .collect(Collectors.groupingBy(
                        view -> view.base().setCode(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}