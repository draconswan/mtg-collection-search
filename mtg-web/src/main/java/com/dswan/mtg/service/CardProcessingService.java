package com.dswan.mtg.service;

import com.dswan.mtg.domain.ParsedCardLine;
import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.entity.CardEntity;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.dto.CardSetDTO;
import com.dswan.mtg.repository.CardRepository;
import com.dswan.mtg.util.CardColorComparator;
import com.dswan.mtg.util.CardNameNormalizer;
import com.dswan.mtg.util.CardProcessingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.Tuple;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.dswan.mtg.util.CardProcessingUtil.*;
import static com.dswan.mtg.util.DeckProcessingUtil.TYPE_ORDER;

@Service
@Slf4j
public class CardProcessingService {

    public static final List<String> SET_TYPE_FILTER = List.of("memorabilia");

    private final CardRepository cardRepository;

    public CardProcessingService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Fetch all printings for a given card name.
     */
    public List<Card> findAllPrintings(String normalizedName) {
        List<CardEntity> allPrintingsForCardName = cardRepository.findAllPrintingsForCardName(normalizedName);
        return allPrintingsForCardName.stream().map(CardMapper::toDomain).toList();
    }

    /**
     * Fetch all printings for a list of card names.
     */
    public List<Card> findAllPrintingsForNames(List<String> names) {
        return names.stream()
                .map(cardRepository::findAllPrintingsForCardName)
                .flatMap(List::stream)
                .map(CardMapper::toDomain)
                .toList();
    }

    public List<ParsedCardLine> parseLines(String rawInput) {
        List<ParsedCardLine> results = new ArrayList<>();

        Arrays.stream(rawInput.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("//"))
                .filter(line -> !line.startsWith("#"))
                .filter(CardProcessingUtil::notSectionHeader)
                .forEach(line -> {
                    int quantity = extractQuantity(line);
                    Optional<String> set = extractSet(line);
                    Optional<String> collector = extractCollectorNumber(line);
                    String cleaned = cleanName(line);

                    if (!cleaned.isEmpty()) {
                        results.add(new ParsedCardLine(quantity, cleaned, set, collector, line));
                    }
                });

        return results;
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
                .collect(Collectors.groupingBy(Card::getSet))
                .entrySet().stream()
                .map(entry -> {
                    List<Card> cardsInSet = entry.getValue();
                    List<Card> sortedCards = cardsInSet.stream()
                            .sorted(Comparator.comparing(Card::getColor, Comparator.nullsLast(new CardColorComparator()))
                                    .thenComparing(Card::getName, String.CASE_INSENSITIVE_ORDER)
                                    .thenComparing(Card::getCollectorNumber))
                            .toList();
                    Card sample = sortedCards.getFirst();
                    List<String> games = Optional.ofNullable(sample.getGamesList())
                            .map(s -> Arrays.asList(s.split(",")))
                            .orElse(List.of());
                    return new CardSetDTO(
                            entry.getKey(),
                            sample.getSetName(),
                            sample.getSetType(),
                            LocalDate.parse(sample.getReleasedAt()),
                            sortedCards,
                            games
                    );
                })
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
    public Tuple<List<CardEntry>, List<String>> buildDecklist(String rawInput) {
        List<ParsedCardLine> parsed = parseLines(rawInput);
        List<CardEntry> entries = new ArrayList<>();
        List<String> cardsNotFound = new ArrayList<>();
        AtomicInteger idx = new AtomicInteger();
        for (ParsedCardLine line : parsed) {
            String normalized = CardNameNormalizer.normalizeCardName(line.name());
            List<Card> matches = findAllPrintings(normalized);
            if (matches.isEmpty()) {
                log.warn("Failed to find card: {}, normalized: {}", line.name(), normalized);
                cardsNotFound.add(line.rawInput());
                continue;
            }
            List<Card> filtered = matches.stream()
                    .filter(c -> !SET_TYPE_FILTER.contains(c.getSetType()))
                    .toList();
            if (filtered.isEmpty()) {
                continue;
            }
            Card chosen = chooseBestPrinting(filtered, line);
            if (chosen != null) {
                entries.add(new CardEntry(idx.getAndIncrement(), line.quantity(), chosen));
            } else {
                log.warn("Failed to find card with name: {} with Set: {} and Collector Number: {}", line.name(), line.set(), line.collectorNumber());
                cardsNotFound.add(line.rawInput());
            }
        }

        Comparator<CardEntry> byType = Comparator.comparingInt(entry -> {
            var type = entry.getCard().getCardTypes().getCardType().getLast();
            int index = TYPE_ORDER.indexOf(type);
            return index >= 0 ? index : TYPE_ORDER.size();
        });

        return new Tuple<>(
                entries.stream()
                        .sorted(byType.thenComparing(e -> e.getCard().getDisplayName(), String.CASE_INSENSITIVE_ORDER))
                        .toList(),
                cardsNotFound);
    }

    static Card chooseBestPrinting(List<Card> candidates, ParsedCardLine line) {
        List<Card> remaining = candidates;

        if (line.set().isPresent()) {
            remaining = remaining.stream()
                    .filter(c -> c.getSet().equalsIgnoreCase(line.set().get()))
                    .toList();
        }

        if (line.collectorNumber().isPresent()) {
            remaining = remaining.stream()
                    .filter(c -> Objects.equals(c.getCollectorNumber(), line.collectorNumber().get()))
                    .toList();
        }

        if (CollectionUtils.size(remaining) > 1) {
            remaining = remaining.stream()
                    .filter(c -> c.getLang().equals("en"))
                    .toList();
        }

        return remaining.isEmpty() ? null : remaining.getFirst();
    }
}