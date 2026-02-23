package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.entity.CardEntity;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    public static final List<String> SET_TYPE_FILTER = List.of("memorabilia");

    private final CardRepository cardRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Card>> searchCards(@RequestParam String cardName) {
        List<CardEntity> allPrintingsForCardName = cardRepository.findAllPrintingsByPartialName(cardName);
        allPrintingsForCardName = allPrintingsForCardName.stream()
                .filter(cardEntity -> !SET_TYPE_FILTER.contains(cardEntity.getSetType()))
                .toList();
        List<CardEntity> uniqueCards = allPrintingsForCardName.stream()
                .collect(Collectors.toMap(
                        cardEntity -> cardEntity.getName().toLowerCase(),
                        cardEntity -> cardEntity,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
        return ResponseEntity.ok(uniqueCards.stream().map(CardMapper::toDomain).toList());
    }
}
