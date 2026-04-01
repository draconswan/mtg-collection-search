package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.DeckFormats;
import com.dswan.mtg.domain.entity.User;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.domain.entity.UserLandGroupReportDto;
import com.dswan.mtg.domain.cards.Deck;
import com.dswan.mtg.dto.UncheckedCardView;
import com.dswan.mtg.service.DeckService;
import com.dswan.mtg.service.UncheckedCardService;
import com.dswan.mtg.util.DeckColorComparator;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final DeckService deckService;
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
        List<Deck> decks = deckService.getDecksForUser(user.getId());
        decks.forEach(Deck::calculateDeckColors);
        decks.sort(new DeckColorComparator());
        model.addAttribute("decks", decks);
        model.addAttribute("pageTitle", "User Decks");
        model.addAttribute("deckCount", decks.size());
        model.addAttribute("deckFormats", DeckFormats.FORMATS);
        return "user/decks";
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
                                  Model model) {
        Long userId = details.getUser().getId();
        Map<String, List<UncheckedCardView>> missingGroupedBySet = uncheckedCardService.getUncheckedCardsGroupedBySet(userId, type);
        model.addAttribute("groupedBySet", missingGroupedBySet);
        return "decks/all-missing-checklist";
    }
}