package com.dswan.mtg.controller;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.CardType;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.util.CardColorComparator;
import com.dswan.mtg.util.CardNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/search")
public class CardChecklistController {
    private static final List<CardType> TYPE_ORDER = List.of(
            CardType.PLANESWALKER, CardType.CREATURE, CardType.INSTANT, CardType.SORCERY, CardType.ARTIFACT, CardType.ENCHANTMENT, CardType.BATTLE, CardType.LAND
    );
    public static final List<String> SET_TYPE_FILTER = List.of("memorabilia");
    private final CardRepository cardRepository;

    public CardChecklistController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping("/input")
    public String showInputForm() {
        return "card-input"; // maps to card-input.html
    }

    @PostMapping("/checklist")
    public String showChecklist(@RequestParam("cardNames") String cardNamesRaw,
                                @RequestParam(required = false) List<String> gameTypes,
                                Model model) {
        List<String> cardNames = Arrays.stream(cardNamesRaw.split("\\r?\\n"))
                .map(line -> line.replaceAll("^\\d+x?\\s+", "")) // remove leading quantity
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
        List<Card> allCards = cardNames.stream()
                .map(cardRepository::findAllPrintingsForCardName)
                .flatMap(List::stream)
                .peek(Card::populateFromJSON)
                .toList();

        List<CardSetDTO> groupedBySet = allCards.stream()
                .collect(Collectors.groupingBy(Card::getSet_code))
                .entrySet().stream()
                .map(entry -> {
                    List<Card> cardsInSet = entry.getValue();
                    List<Card> uniqueCards = cardsInSet.stream()
                            .collect(Collectors.toMap(
                                    Card::getName, // key: card name
                                    card -> card,  // value: first occurrence
                                    (existing, _) -> existing // keep first
                            ))
                            .values().stream()
                            .sorted(Comparator.comparing(Card::getColor, new CardColorComparator())
                                    .thenComparing(Card::getName, String.CASE_INSENSITIVE_ORDER))
                            .toList();

                    Card sample = uniqueCards.getFirst();
                    return new CardSetDTO(
                            entry.getKey(),
                            sample.getSet_name(),
                            sample.getSet_type(),
                            LocalDate.parse(sample.getReleased_at()),
                            uniqueCards,
                            Arrays.asList(sample.getGamesList().split(","))
                    );
                })
                .sorted(Comparator.comparing(CardSetDTO::getSetDate).reversed())
                .filter(cardSetDTO -> !SET_TYPE_FILTER.contains(cardSetDTO.getSetType()))
                .filter(cardSetDTO -> cardSetDTO.getGameTypes().stream().anyMatch(gameTypes::contains))
                .toList();

        model.addAttribute("cardSets", groupedBySet);
        model.addAttribute("cardTypes", CardType.values());
        return "checklist";
    }

    @PostMapping("/decklist")
    public String showDecklist(@RequestParam("cardNames") String cardNamesRaw,
                               Model model) {
        List<CardEntry> cardEntries = Arrays.stream(cardNamesRaw.split("\\r?\\n"))
                .map((String line) -> {
                    String[] parts = line.split("\\s+", 2);
                    int quantity;
                    String name;

                    if (parts[0].matches("\\d+")) {
                        quantity = Integer.parseInt(parts[0]);
                        name = parts.length > 1 ? parts[1] : "Unknown";
                    } else if (parts[0].matches("\\d+x")) {
                        quantity = Integer.parseInt(parts[0].replace("x", ""));
                        name = parts.length > 1 ? parts[1] : "Unknown";
                    } else {
                        quantity = 1;
                        name = line;
                    }

                    if (!StringUtils.isEmpty(name) && !name.equals("Unknown")) {
                        String normalizedName = CardNameNormalizer.normalizeCardName(name);
                        List<Card> matches = cardRepository.findAllPrintingsForCardName(normalizedName);
                        if (matches.isEmpty()) {
                            log.warn("Failed to find card with name: {}, normalized: {}", name, normalizedName);
                        }
                        Card card = matches.isEmpty() ? null : matches.stream().filter(card1 -> !SET_TYPE_FILTER.contains(card1.getSet_type())).findFirst().orElse(null);
                        return new CardEntry(quantity, card);
                    } else {
                        return new CardEntry(quantity, null);
                    }
                })
                .filter(cardEntry -> cardEntry.getCard() != null)
                .sorted(
                        Comparator.comparing(
                                (CardEntry entry) -> entry.getCard().getCard_types().getCardType().getLast(),
                                Comparator.comparingInt(type -> {
                                    int index = TYPE_ORDER.indexOf(type);
                                    return index >= 0 ? index : TYPE_ORDER.size();
                                })
                        ).thenComparing(entry -> entry.getCard().getName(), String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
        Map<String, List<CardEntry>> groupedDecklist = new LinkedHashMap<>();
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        for (CardEntry entry : cardEntries) {
            String type = entry.getCard().getCard_types().getCardType().getLast().toString();
            groupedDecklist.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            typeQuantities.merge(type, entry.getQuantity(), Integer::sum);
        }
        int totalQuantity = typeQuantities.values().stream().mapToInt(Integer::intValue).sum();
        model.addAttribute("groupedDecklist", groupedDecklist);
        model.addAttribute("typeQuantities", typeQuantities);
        model.addAttribute("totalQuantity", totalQuantity);
        return "decklist";
    }
}