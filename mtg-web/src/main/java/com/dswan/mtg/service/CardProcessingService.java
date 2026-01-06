package com.dswan.mtg.service;

import com.dswan.mtg.domain.ParsedCardLine;
import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.cards.CardType;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.util.CardColorComparator;
import com.dswan.mtg.util.CardNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardProcessingService {
    private static final List<CardType> TYPE_ORDER = List.of(
            CardType.PLANESWALKER, CardType.CREATURE, CardType.INSTANT, CardType.SORCERY,
            CardType.ARTIFACT, CardType.ENCHANTMENT, CardType.BATTLE, CardType.LAND
    );

    private static final List<String> SET_TYPE_FILTER = List.of("memorabilia");

    private static final List<String> SECTION_HEADERS = List.of(
            "sideboard", "sb:", "commander", "companion",
            "maybeboard", "maybe:", "tokens", "custom", "categories"
    );

    private final CardRepository cardRepository;

    public CardProcessingService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Fetch all printings for a given card name.
     */
    public List<Card> findAllPrintings(String normalizedName) {
        return cardRepository.findAllPrintingsForCardName(normalizedName);
    }

    /**
     * Fetch all printings for a list of card names.
     */
    public List<Card> findAllPrintingsForNames(List<String> names) {
        return names.stream()
                .map(cardRepository::findAllPrintingsForCardName)
                .flatMap(List::stream)
                .toList();
    }

    public List<ParsedCardLine> parseLines(String rawInput) {
        List<ParsedCardLine> results = new ArrayList<>();

        Arrays.stream(rawInput.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("//"))
                .filter(line -> !line.startsWith("#"))
                .filter(CardProcessingService::notSectionHeader)
                .forEach(line -> {
                    int quantity = extractQuantity(line);
                    Optional<String> set = extractSet(line);
                    Optional<String> collector = extractCollectorNumber(line);
                    String cleaned = cleanName(line);

                    if (!cleaned.isEmpty()) {
                        results.add(new ParsedCardLine(quantity, cleaned, set, collector));
                    }
                });

        return results;
    }

    private static boolean notSectionHeader(String line) {
        String lower = line.toLowerCase();
        return SECTION_HEADERS.stream().noneMatch(lower::startsWith);
    }

    private static int extractQuantity(String line) {
        String[] parts = line.split("\\s+", 2);
        if (parts[0].matches("\\d+")) return Integer.parseInt(parts[0]);
        if (parts[0].matches("\\d+x")) return Integer.parseInt(parts[0].replace("x", ""));
        return 1;
    }

    private static Optional<String> extractSet(String line) {
        var matcher = Pattern.compile("[\\[(]([A-Za-z0-9]{2,5})[\\])]")
                .matcher(line);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private static Optional<String> extractCollectorNumber(String line) {
        var matcher = Pattern.compile("\\b#?(\\d{1,4})\\b$").matcher(line);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private static String cleanName(String line) {
        line = line.replaceAll("^\\d+x?\\s+", "");
        line = line.replaceAll("(?i)^sb:\\s*", "");
        line = line.replaceAll("(?i)^sideboard:\\s*", "");
        line = line.replaceAll("(?i)^commander:\\s*", "");
        line = line.replaceAll("(?i)^companion:\\s*", "");
        line = line.replaceAll("[\\[(][A-Za-z0-9]{2,5}[\\])]\\s*", "");
        line = line.replaceAll("\\b#?\\d{1,4}\\b$", "");
        line = line.replaceAll("(?i)\\b(foil|etched|retro|showcase|borderless|extended art|promo)\\b", "");
        line = line.replaceAll("\\(.*?\\)", "");
        return line.trim();
    }

    // ------------------------------------------------------------
    // 2. Checklist builder (names only)
    // ------------------------------------------------------------
    public List<String> extractNamesOnly(String rawInput) {
        return parseLines(rawInput).stream()
                .map(ParsedCardLine::name)
                .toList();
    }

    public List<CardSetDTO> buildChecklist(List<Card> allCards, List<String> selectedGameTypes) {
        return allCards.stream()
                .collect(Collectors.groupingBy(Card::getSet_code))
                .entrySet().stream()
                .map(entry -> {
                    List<Card> cardsInSet = entry.getValue();
                    List<Card> uniqueCards = cardsInSet.stream()
                            .sorted(Comparator.comparing(Card::getName, String.CASE_INSENSITIVE_ORDER))
                            .collect(Collectors.toMap(
                                    Card::getName,
                                    card -> card,
                                    (existing, _) -> existing,
                                    LinkedHashMap::new
                            ))
                            .values().stream()
                            .sorted(Comparator.comparing(Card::getColor, Comparator.nullsLast(new CardColorComparator()))
                                    .thenComparing(Card::getName, String.CASE_INSENSITIVE_ORDER))
                            .toList();
                    if (uniqueCards.isEmpty()) {
                        return null;
                    }
                    Card sample = uniqueCards.getFirst();
                    List<String> games = Optional.ofNullable(sample.getGamesList())
                            .map(s -> Arrays.asList(s.split(",")))
                            .orElse(List.of());
                    return new CardSetDTO(
                            entry.getKey(),
                            sample.getSet_name(),
                            sample.getSet_type(),
                            LocalDate.parse(sample.getReleased_at()),
                            uniqueCards,
                            games
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CardSetDTO::getSetDate).reversed())
                .filter(dto -> !SET_TYPE_FILTER.contains(dto.getSetType()))
                .filter(dto -> dto.getGameTypes().stream()
                        .map(String::toLowerCase)
                        .anyMatch(selectedGameTypes::contains))
                .toList();
    }

    // ------------------------------------------------------------
    // 3. Decklist builder
    // ------------------------------------------------------------
    public List<CardEntry> buildDecklist(String rawInput) {
        List<ParsedCardLine> parsed = parseLines(rawInput);
        List<CardEntry> entries = new ArrayList<>();
        for (ParsedCardLine line : parsed) {
            String normalized = CardNameNormalizer.normalizeCardName(line.name());
            List<Card> matches = findAllPrintings(normalized);
            if (matches.isEmpty()) {
                log.warn("Failed to find card: {}, normalized: {}", line.name(), normalized);
                continue;
            }
            List<Card> filtered = matches.stream()
                    .filter(c -> !SET_TYPE_FILTER.contains(c.getSet_type()))
                    .toList();
            if (filtered.isEmpty()) {
                continue;
            }
            Card chosen = chooseBestPrinting(filtered, line);
            if (chosen != null) {
                entries.add(new CardEntry(line.quantity(), chosen));
            }
        }

        Comparator<CardEntry> byType = Comparator.comparingInt(entry -> {
            var type = entry.getCard().getCard_types().getCardType().getLast();
            int index = TYPE_ORDER.indexOf(type);
            return index >= 0 ? index : TYPE_ORDER.size();
        });

        return entries.stream()
                .sorted(byType.thenComparing(e -> e.getCard().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Card chooseBestPrinting(List<Card> candidates, ParsedCardLine line) {
        List<Card> remaining = candidates;

        if (line.set().isPresent()) {
            remaining = remaining.stream()
                    .filter(c -> c.getSet_code().equalsIgnoreCase(line.set().get()))
                    .toList();
        }

        if (line.collectorNumber().isPresent()) {
            remaining = remaining.stream()
                    .filter(c -> Objects.equals(c.getCollector_number(), line.collectorNumber().get()))
                    .toList();
        }

        return remaining.isEmpty() ? null : remaining.getFirst();
    }
}