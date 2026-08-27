package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.*;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.domain.model.GlobalCheckedStateForm;
import com.dswan.mtg.model.DeckViewModel;
import com.dswan.mtg.service.DeckBuilderService;
import com.dswan.mtg.service.DeckService;
import com.dswan.mtg.util.DeckProcessingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/user")
public class DeckManagementController {
    private final DeckService deckService;
    private final DeckBuilderService deckBuilderService;

    @GetMapping("/deck/new")
    public String newDeck(Model model) {
        return "redirect:/search/input";
    }

    @GetMapping("/deck/{deckId}")
    public String showSavedDeck(@PathVariable String deckId, Model model) {
        Deck deck = deckService.getDeck(deckId);
        DeckViewModel vm = DeckViewModel.buildDeckViewModel(deck);
        model.addAttribute("deckView", vm);
        return "decks/decklist";
    }

    @PostMapping("/deck/save-deck-state")
    public String saveDeckState(@ModelAttribute DeckStateForm form) {
        Deck deck = deckBuilderService.buildDeck(form);
        Deck saved = deckService.saveDeck(deck);
        return "redirect:/user/deck/" + saved.getId();
    }

    @RequestMapping(value = "/deck/{deckId}/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteDeck(@PathVariable String deckId) {
        deckService.deleteDeck(deckId);
        return "redirect:/user/decks";
    }
}
