package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ResponseEntity<List<Deck>> getDecksForUser(@RequestParam Long userId) {
        List<Deck> decks = deckService.getDecksForUser(userId);
        return ResponseEntity.ok(decks);
    }

    @PostMapping
    public ResponseEntity<Deck> saveDeck(@RequestBody Deck deck) {
        Deck created = deckService.saveDeck(deck);
        return ResponseEntity
                .created(URI.create("/api/v1/decks/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<Deck> getDeck(@PathVariable String deckId) {
        Deck deck = deckService.getDeck(deckId);
        return ResponseEntity.ok(deck);
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable String deckId) {
        deckService.deleteDeck(deckId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deckId}/remove-card")
    @ResponseBody
    public ResponseEntity<Boolean> removeCard(@PathVariable String deckId, @RequestParam String cardId) {
        Boolean success = deckService.removeCardFromDeck(deckId, cardId);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{deckId}/add-card")
    @ResponseBody
    public ResponseEntity<Boolean> addCard(@PathVariable String deckId, @RequestParam String cardId) {
        Boolean success = deckService.addCardToDeck(deckId, cardId);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{deckId}/update-quantity")
    @ResponseBody
    public ResponseEntity<Boolean> updateCardQuantity(@PathVariable String deckId, @RequestParam String cardId, @RequestParam Integer quantity) {
        Boolean success = deckService.updateDeckCardQuantity(deckId, cardId, quantity);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{deckId}/update-checked")
    @ResponseBody
    public ResponseEntity<Boolean> updateCardQuantity(@PathVariable String deckId, @RequestParam String cardId, @RequestParam Boolean checked) {
        Boolean success = deckService.updateDeckCardChecked(deckId, cardId, checked);
        return ResponseEntity.ok(success);
    }
}