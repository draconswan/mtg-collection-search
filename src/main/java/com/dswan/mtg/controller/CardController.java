package com.dswan.mtg.controller;

import com.dswan.mtg.dto.CardListRequest;
import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.dto.UpdateResult;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.service.DatabasePopulationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
public class CardController {
    private final DatabasePopulationService databasePopulationService;
    private final CardRepository cardRepository;

    @GetMapping("/refresh")
    public ResponseEntity<UpdateResult> refreshDatabase(@RequestParam(defaultValue = "false") boolean force) {
        UpdateResult result = databasePopulationService.checkAndUpdateDatabase(force);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    @PostMapping("/printings")
    public ResponseEntity<List<CardSetDTO>> getCardPrintings(@RequestBody CardListRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getCardNames())) {
            return ResponseEntity.noContent().build();
        }
        List<Card> allCards = request.getCardNames().stream()
                .map(cardRepository::findAllPrintingsForCardName)
                .flatMap(List::stream)
                .toList();

        List<CardSetDTO> groupedBySet = allCards.stream()
                .collect(Collectors.groupingBy(Card::getSet_code))
                .entrySet().stream()
                .map(entry -> {
                    List<Card> cardsInSet = entry.getValue();
                    Card sample = cardsInSet.getFirst(); // assuming all cards in the set share setName and setType
                    return new CardSetDTO(
                            entry.getKey(),
                            sample.getSet_name(),
                            sample.getSet_type(),
                            LocalDate.parse(sample.getReleased_at()),
                            cardsInSet,
                            Arrays.asList(sample.getGamesList().split(","))
                    );
                })
                .toList();

        return ResponseEntity.ok(groupedBySet);
    }
}
