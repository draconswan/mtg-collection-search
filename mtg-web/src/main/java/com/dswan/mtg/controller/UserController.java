package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.CardType;
import com.dswan.mtg.domain.entity.User;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.service.DeckBuilderService;
import com.dswan.mtg.service.DeckService;
import com.dswan.mtg.util.DeckProcessingUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dswan.mtg.util.DeckProcessingUtil.TYPE_ORDER;

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
        AtomicInteger idx = new AtomicInteger();
        List<CardEntry> cardEntries = deck.getCards().stream()
                .map(card -> new CardEntry(idx.getAndIncrement(), card.getQuantity(), card))
                .toList();
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        Map<String, List<CardEntry>> orderedGroups = DeckProcessingUtil.getOrderedCardGroups(cardEntries, typeQuantities);
        int totalQuantity = typeQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        DeckStateForm form = new DeckStateForm();
        form.setDeckId(deckId);
        form.setDeckName(deck.getName());
        form.setDeckFormat(deck.getType());
        form.setCards(deck.getCards()
                .stream()
                .map(card -> {
                    CardStateForm cardStateForm = new CardStateForm();
                    cardStateForm.setCardId(card.getId());
                    cardStateForm.setQuantity(card.getQuantity());
                    cardStateForm.setChecked(card.isChecked());
                    return cardStateForm;
                })
                .toList());
        model.addAttribute("groupedDecklist", orderedGroups);
        model.addAttribute("typeQuantities", typeQuantities);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("deckName", deck.getName());
        model.addAttribute("deckFormat", deck.getType());
        model.addAttribute("pageTitle", deck.getName());
        model.addAttribute("cardsNotFound", List.of());
        model.addAttribute("deckId", deckId);
        model.addAttribute("deckStateForm", form);
        return "decks/decklist";
    }

    @PostMapping("/deck/save-deck-state")
    public String saveDeckState(@ModelAttribute DeckStateForm form) {
        Deck deck = deckBuilderService.buildDeck(form);
        Deck saved = deckService.saveDeck(deck);
        return "redirect:/user/deck/" + saved.getId();
    }

    @RequestMapping(value = "/deck/{deckId}/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
        return "redirect:/user/decks";
    }
}