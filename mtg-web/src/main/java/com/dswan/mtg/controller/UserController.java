package com.dswan.mtg.controller;

import com.dswan.mtg.domain.entity.User;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.service.DeckBuilderService;
import com.dswan.mtg.service.DeckService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final DeckService deckService;
    private final DeckBuilderService deckBuilderService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "login";
    }

    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("pageTitle", "Registration");
        return "register";
    }

    @GetMapping("/decks")
    public String decks(@AuthenticationPrincipal UserDetailsDto details, Model model) {
        User user = details.getUser();
        List<Deck> decks = deckService.getDecksForUser(user.getId());
        model.addAttribute("decks", decks);
        model.addAttribute("pageTitle", "User Decks");
        return "user/decks";
    }

    @GetMapping("/deck/new")
    public String newDeck(Model model) {
        return "redirect:search/input";
    }

    @GetMapping("/deck/{deckId}")
    public String showSavedDeck(@PathVariable Long deckId, Model model) {
        Deck deck = deckService.getDeck(deckId);
        // 1. Build flat list with explicit indexes
        AtomicInteger idx = new AtomicInteger();
        List<CardEntry> cardEntries = deck.getCards().stream()
                .map(card -> new CardEntry(idx.getAndIncrement(), card.getQuantity(), card)) // or card.getQuantity() if you store quantity
                .toList();
        Map<String, List<CardEntry>> groupedDecklist = new LinkedHashMap<>();
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        for (CardEntry entry : cardEntries) {
            String type = entry.getCard()
                    .getCardTypes()
                    .getCardType()
                    .getLast()
                    .toString();

            groupedDecklist.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            typeQuantities.merge(type, entry.getQuantity(), Integer::sum);
        }
        int totalQuantity = typeQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        model.addAttribute("groupedDecklist", groupedDecklist);
        model.addAttribute("typeQuantities", typeQuantities);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("pageTitle", deck.getName());
        model.addAttribute("cardsNotFound", List.of()); // none when loading from DB
        model.addAttribute("deckId", deckId);
        return "decks/decklist";
    }

    @PostMapping("/deck/save-deck-state")
    public String saveDeckState(@ModelAttribute DeckStateForm form) {
        Deck deck = deckBuilderService.buildDeck(form);
        Deck saved = deckService.createDeck(deck);
        return "redirect:/user/deck/" + saved.getId();
    }

    @RequestMapping(value = "/deck/{deckId}/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
        return "redirect:/user/decks";
    }
}