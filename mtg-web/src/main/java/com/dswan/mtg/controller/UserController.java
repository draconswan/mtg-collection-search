package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.entity.User;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.domain.entity.UserLandGroupReportDto;
import com.dswan.mtg.dto.DeckSummaryView;
import com.dswan.mtg.dto.UncheckedCardView;
import com.dswan.mtg.service.DeckService;
import com.dswan.mtg.service.UncheckedCardService;
import com.dswan.mtg.service.UserCollectionService;
import com.dswan.mtg.util.DeckColorComparator;
import com.dswan.mtg.util.DeckTypeComparator;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final DeckService deckService;
    private final UserCollectionService userCollectionService;
    private final UncheckedCardService uncheckedCardService;

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
        List<DeckSummaryView> decks = deckService.getDecksSummaryForUser(user.getId());
        decks = decks.stream().map(d -> new DeckSummaryView(
                        d.id(),
                        d.name(),
                        d.type(),
                        d.totalCards(),
                        d.checkedCount(),
                        d.proxyCount(),
                        d.deckColors(),
                        d.computeBadgeClass(d)
                ))
                .sorted(new DeckColorComparator<>())
                .sorted(new DeckTypeComparator<>())
                .toList();
        model.addAttribute("decks", decks);
        model.addAttribute("pageTitle", "User Decks");
        model.addAttribute("deckCount", decks.size());
        model.addAttribute("deckFormats", DeckFormats.FORMATS);
        return "user/decks";
    }

    @GetMapping("/collection")
    public String collection(@AuthenticationPrincipal UserDetailsDto details, Model model) {
        User user = details.getUser();
        var collectionOverview = userCollectionService.getUserCollectionOverview(user.getId());
        model.addAttribute("overview", collectionOverview);
        model.addAttribute("pageTitle", user.getUsername() + "'s Collection");
        model.addAttribute("deckFormats", DeckFormats.FORMATS);
        return "user/collection";
    }

    @GetMapping("/decks/land-audit")
    public String landAudit(@AuthenticationPrincipal UserDetailsDto details, Model model) {
        User user = details.getUser();
        List<UserLandGroupReportDto> audit = deckService.getLandAuditForUser(user.getId());
        model.addAttribute("landAudit", audit);
        model.addAttribute("pageTitle", "User Land Audit Report");
        return "decks/land-audit";
    }

    @GetMapping("/decks/all-missing")
    public String allMissingCards(@AuthenticationPrincipal UserDetailsDto details,
                                  @RequestParam(required = false) List<String> type,
                                  @RequestParam(required = false) List<UUID> deckIds,
                                  @RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "10") Integer numSets,
                                  Model model) {
        Long userId = details.getUser().getId();
        AtomicReference<Integer> pagesRef = new AtomicReference<>();
        Map<String, List<UncheckedCardView>> missingGroupedBySet = uncheckedCardService.getUncheckedCardsGroupedBySet(userId, type, deckIds, page, numSets, pagesRef);
        model.addAttribute("groupedBySet", missingGroupedBySet);
        model.addAttribute("page", page);
        model.addAttribute("numSets", numSets);
        model.addAttribute("types", type);
        model.addAttribute("deckIds", deckIds);
        model.addAttribute("totalPages", pagesRef.get());
        return "decks/all-missing-checklist";
    }
}