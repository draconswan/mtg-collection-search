package com.dswan.mtg.controller;

import com.dswan.mtg.dto.UpdateResult;
import com.dswan.mtg.service.DatabasePopulationService;
import com.dswan.mtg.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
public class ApiController {
    private final DatabasePopulationService databasePopulationService;
    private final DeckService deckService;

    @GetMapping("/refresh")
    public ResponseEntity<UpdateResult> refreshDatabase(@RequestParam(defaultValue = "false") boolean force) {
        UpdateResult result = databasePopulationService.checkAndUpdateDatabase(force);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
