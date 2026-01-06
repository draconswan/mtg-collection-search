package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.CardType;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.service.CardProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@Slf4j
@RequestMapping("/search")
public class SearchController {
    private final CardProcessingService cardProcessingService;

    public SearchController(CardProcessingService cardProcessingService) {
        this.cardProcessingService = cardProcessingService;
    }

    @GetMapping("/input")
    public String showInputForm(Model model) {
        model.addAttribute("pageTitle", "Card List");
        return "card-input";
    }

    @PostMapping("/checklist")
    public String showChecklist(@RequestParam("cardNames") String cardNamesRaw,
                                @RequestParam(required = false) List<String> gameTypes,
                                Model model) {
        List<String> selectedGameTypes = (gameTypes == null || gameTypes.isEmpty()) ? List.of("paper") : gameTypes.stream().map(String::toLowerCase).toList();
        List<String> cardNames = cardProcessingService.extractNamesOnly(cardNamesRaw);
        List<Card> allCards = cardProcessingService.findAllPrintingsForNames(cardNames)
                .stream()
                .map(card -> {
                    card.populateFromJSON();
                    return card;
                })
                .toList();
        List<CardSetDTO> groupedBySet = cardProcessingService.buildChecklist(allCards, selectedGameTypes);
        model.addAttribute("cardSets", groupedBySet);
        model.addAttribute("cardTypes", CardType.values());
        model.addAttribute("pageTitle", "Search Checklist");
        return "checklist";
    }

    @PostMapping("/decklist")
    public String showDecklist(@RequestParam("cardNames") String cardNamesRaw,
                               Model model) {
        List<CardEntry> cardEntries = cardProcessingService.buildDecklist(cardNamesRaw);
        Map<String, List<CardEntry>> groupedDecklist = new LinkedHashMap<>();
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        for (CardEntry entry : cardEntries) {
            String type = entry.getCard().getCard_types().getCardType().getLast().toString();
            groupedDecklist.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            typeQuantities.merge(type, entry.getQuantity(), Integer::sum);
        }
        int totalQuantity = typeQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        model.addAttribute("groupedDecklist", groupedDecklist);
        model.addAttribute("typeQuantities", typeQuantities);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("pageTitle", "Decklist");
        return "decklist";
    }
}