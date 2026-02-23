package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.CardType;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.service.CardProcessingService;
import com.dswan.mtg.util.DeckProcessingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.util.Tuple;

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
        return "search/checklist";
    }

    @PostMapping("/decklist")
    public String showDecklist(@RequestParam("cardNames") String cardNamesRaw,
                               Model model) {
        Tuple<List<CardEntry>, List<String>> cardEntriesAndErrors = cardProcessingService.buildDecklist(cardNamesRaw);
        List<CardEntry> cardEntries = cardEntriesAndErrors._1();
        List<String> cardsNotFound = cardEntriesAndErrors._2();
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        Map<String, List<CardEntry>> orderedGroups = DeckProcessingUtil.getOrderedCardGroups(cardEntries, typeQuantities);
        int totalQuantity = typeQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        DeckStateForm form = new DeckStateForm();
        form.setCards(cardEntries.stream().map(cardEntry -> {
                            CardStateForm cardStateForm = new CardStateForm();
                            cardStateForm.setCardId(cardEntry.getCard().getId());
                            cardStateForm.setQuantity(cardEntry.getQuantity());
                            cardStateForm.setChecked(false);
                            return cardStateForm;
                        })
                        .toList()
        );
        model.addAttribute("groupedDecklist", orderedGroups);
        model.addAttribute("typeQuantities", typeQuantities);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("pageTitle", "Decklist");
        model.addAttribute("cardsNotFound", cardsNotFound);
        model.addAttribute("deckStateForm", form);
        return "decks/decklist";
    }
}