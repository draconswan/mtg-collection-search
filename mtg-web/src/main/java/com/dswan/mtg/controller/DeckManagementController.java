package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.domain.model.GlobalCheckedStateForm;
import com.dswan.mtg.service.DeckBuilderService;
import com.dswan.mtg.service.DeckService;
import com.dswan.mtg.util.DeckProcessingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/user")
public class DeckManagementController {
    private final DeckService deckService;
    private final DeckBuilderService deckBuilderService;

    @GetMapping("/deck/new")
    public String newDeck(Model model) {
        return "redirect:search/input";
    }

    @GetMapping("/deck/{deckId}")
    public String showSavedDeck(@PathVariable String deckId, Model model) {
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
        Long totalProxies = cardEntries.stream()
                .filter(c -> c.getCard().isProxy())
                .count();
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
        model.addAttribute("totalProxies", totalProxies);
        model.addAttribute("deckName", deck.getName());
        model.addAttribute("deckFormat", deck.getType());
        model.addAttribute("pageTitle", deck.getName());
        model.addAttribute("cardsNotFound", List.of());
        model.addAttribute("deckId", deckId);
        model.addAttribute("deckStateForm", form);
        model.addAttribute("deckFormats", DeckFormats.FORMATS);
        return "decks/decklist";
    }

    @PostMapping("/deck/save-deck-state")
    public String saveDeckState(@ModelAttribute DeckStateForm form) {
        Deck deck = deckBuilderService.buildDeck(form);
        Deck saved = deckService.saveDeck(deck);
        return "redirect:/user/deck/" + saved.getId();
    }

    @PostMapping("/decks/update-all")
    public String updateAllChecked(@ModelAttribute GlobalCheckedStateForm form) {
        form.getCards().forEach(entry ->
                deckService.updateDeckCardChecked(
                        entry.getDeckId(),
                        entry.getCardId(),
                        entry.isChecked()
                )
        );
        return "redirect:/user/decks/all-missing";
    }

    @RequestMapping(value = "/deck/{deckId}/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteDeck(@PathVariable String deckId) {
        deckService.deleteDeck(deckId);
        return "redirect:/user/decks";
    }
}
