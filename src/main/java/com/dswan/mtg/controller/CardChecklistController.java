package com.dswan.mtg.controller;

import com.dswan.mtg.domain.Card;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.repository.CardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CardChecklistController {

    private final CardRepository cardRepository;

    public CardChecklistController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping("/input")
    public String showInputForm() {
        return "card-input"; // maps to card-input.html
    }

    @PostMapping("/checklist")
    public String showChecklist(@RequestParam("cardNames") String cardNamesRaw,
                                @RequestParam(required = false) List<String> gameTypes,
                                Model model) {
        List<String> cardNames = Arrays.stream(cardNamesRaw.split("\\r?\\n"))
                .map(line -> line.replaceAll("^\\d+x?\\s+", "")) // remove leading quantity
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
        List<Card> allCards = cardNames.stream()
                .map(cardRepository::findAllPrintingsForCardName)
                .flatMap(List::stream)
                .toList();

        List<CardSetDTO> groupedBySet = allCards.stream()
                .collect(Collectors.groupingBy(Card::getSet_code))
                .entrySet().stream()
                .map(entry -> {
                    List<Card> cardsInSet = entry.getValue();
                    List<Card> uniqueCards = cardsInSet.stream()
                            .collect(Collectors.toMap(
                                    Card::getName, // key: card name
                                    card -> card,  // value: first occurrence
                                    (existing, _) -> existing // keep first
                            ))
                            .values().stream()
                            .toList();

                    Card sample = uniqueCards.getFirst();
                    return new CardSetDTO(
                            entry.getKey(),
                            sample.getSet_name(),
                            sample.getSet_type(),
                            LocalDate.parse(sample.getReleased_at()),
                            uniqueCards,
                            Arrays.asList(sample.getGamesList().split(","))
                    );
                })
                .sorted(Comparator.comparing(CardSetDTO::getSetDate).reversed())
                .filter(cardSetDTO -> {
                    List<String> setTypeFilter = Arrays.asList("promo", "memorabilia");
                    return !setTypeFilter.contains(cardSetDTO.getSetType());
                })
                .filter(cardSetDTO -> cardSetDTO.getGameTypes().stream().anyMatch(gameTypes::contains))
                .toList();

        model.addAttribute("cardSets", groupedBySet);
        return "checklist";
    }
}