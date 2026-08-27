package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.*;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.service.CardProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        return "search/card-input";
    }

    @PostMapping("/checklist")
    public String showChecklist(@RequestParam("cardNames") String cardNamesRaw,
                                @RequestParam(required = false) List<String> gameTypes,
                                @AuthenticationPrincipal UserDetailsDto userDetails,
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
        Object groupedBySet = cardProcessingService.buildChecklist(allCards, selectedGameTypes, userDetails.getUser().getSortType());
        model.addAttribute("cardSets", groupedBySet);
        model.addAttribute("cardTypes", CardType.values());
        model.addAttribute("pageTitle", "Search Checklist");
        return "search/checklist";
    }

    @PostMapping("/decklist")
    public String showDecklist(@RequestParam("cardNames") String cardNamesRaw,
                               Model model) {
        model.addAttribute("deckView", cardProcessingService.buildDecklist(cardNamesRaw));
        return "decks/decklist";
    }
}