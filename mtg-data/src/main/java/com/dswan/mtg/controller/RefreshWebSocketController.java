package com.dswan.mtg.controller;

import com.dswan.mtg.dto.RefreshRequest;
import com.dswan.mtg.dto.StatusMessage;
import com.dswan.mtg.dto.UpdateResult;
import com.dswan.mtg.service.DatabasePopulationService;
import com.dswan.mtg.service.ScryfallSetSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
public class RefreshWebSocketController {

    private final DatabasePopulationService databasePopulationService;
    private final ScryfallSetSyncService scryfallSetSyncService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/refresh")
    public void refreshDatabase(RefreshRequest request, Principal principal) {
        if (!(principal instanceof Authentication auth)) {
            // no auth at all → reject
            return;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // not an admin → reject
            return;
        }
        // Run async so we don't block the WebSocket thread
        CompletableFuture.runAsync(() -> {
            messagingTemplate.convertAndSend("/topic/refresh-status", new StatusMessage("Starting refresh"));
            UpdateResult result = databasePopulationService.checkAndUpdateDatabase(request.isForce());
            messagingTemplate.convertAndSend("/topic/refresh-status", new StatusMessage("Card data updated"));
            if (request.isUpdateSets()) {
                messagingTemplate.convertAndSend("/topic/refresh-status", new StatusMessage("Updating set metadata"));
                scryfallSetSyncService.syncSets();
                messagingTemplate.convertAndSend("/topic/refresh-status", new StatusMessage("Set metadata updated."));
            }
            messagingTemplate.convertAndSend("/topic/refresh-status", result); // final result object
        });
    }
}