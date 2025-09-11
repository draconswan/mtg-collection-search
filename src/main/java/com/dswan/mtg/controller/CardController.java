package com.dswan.mtg.controller;

import com.dswan.mtg.domain.Card;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.service.DatabasePopulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
public class CardController {
    private final DatabasePopulationService databasePopulationService;
    private final CardRepository cardRepository;

    @GetMapping("/status")
    public ResponseEntity getDatabaseStatus() throws Exception {
        databasePopulationService.checkAndUpdateDatabase(false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/printings")
    public ResponseEntity<Map<String, List<Card>>> getCardPrintings(@RequestParam("cardName") List<String> cardNames){
        Map<String, List<Card>> results = new HashMap<>();
        for (String name : cardNames){
            List<Card> allPrintingsForCardName = cardRepository.findAllPrintingsForCardName(name);
            results.put(name, allPrintingsForCardName);
        }
        return ResponseEntity.ok(results);
    }
}
