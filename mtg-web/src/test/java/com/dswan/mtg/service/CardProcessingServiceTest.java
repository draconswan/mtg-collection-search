package com.dswan.mtg.service;

import com.dswan.mtg.domain.ParsedCardLine;
import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.entity.CardEntity;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.util.Tuple;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CardProcessingServiceTest {

    private final CardRepository repo = mock(CardRepository.class);
    private final CardProcessingService service = new CardProcessingService(repo);

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private Card card(
            String name,
            String set,
            String setName,
            String setType,
            String releasedAt,
            String color,
            String collectorNumber,
            String gamesList
    ) {
        Card c = new Card();
        c.setName(name);
        c.setSet(set);
        c.setSetName(setName);
        c.setSetType(setType);
        c.setReleasedAt(releasedAt);
        c.setColor(color);
        c.setCollectorNumber(collectorNumber);
        c.setGamesList(gamesList);
        return c;
    }

    private CardEntity entity() {
        return new CardEntity();
    }

    // ------------------------------------------------------------
    // parseLines
    // ------------------------------------------------------------

    @Test
    @DisplayName("parseLines filters comments, blanks, and section headers")
    void parseLines_filtersNoise() {
        String input = """
                // comment
                # also comment
                SB: 1 Sol Ring
                1 Lightning Bolt (2XM) 350 *F*
                """;

        var result = service.parseLines(input);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(line -> {
                    assertThat(line.quantity()).isEqualTo(1);
                    assertThat(line.name()).isEqualTo("Lightning Bolt");
                    assertThat(line.set()).contains("2xm");
                    assertThat(line.collectorNumber()).contains("350");
                    assertThat(line.rawInput()).contains("Lightning Bolt");
                });
    }

    @Test
    @DisplayName("parseLines handles multiple valid lines")
    void parseLines_multiple() {
        String input = """
                1 Sol Ring (CMM) 123
                2 Brainstorm (ICE) 72 *E*
                """;

        var result = service.parseLines(input);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Sol Ring");
        assertThat(result.get(1).name()).isEqualTo("Brainstorm");
    }

    // ------------------------------------------------------------
    // extractNamesOnly
    // ------------------------------------------------------------

    @Test
    void extractNamesOnly_returnsJustNames() {
        String input = """
                1 Sol Ring (CMM) 123
                2 Brainstorm (ICE) 72 *E*
                """;

        var names = service.extractNamesOnly(input);

        assertThat(names).containsExactly("Sol Ring", "Brainstorm");
    }

    // ------------------------------------------------------------
    // findAllPrintings
    // ------------------------------------------------------------

    @Test
    void findAllPrintings_mapsEntitiesToDomain() {
        CardEntity e1 = entity();
        CardEntity e2 = entity();

        when(repo.findAllPrintingsForCardName("sol ring"))
                .thenReturn(List.of(e1, e2));

        var result = service.findAllPrintings("sol ring");

        assertThat(result)
                .containsExactly(CardMapper.toDomain(e1), CardMapper.toDomain(e2));
    }

    // ------------------------------------------------------------
    // findAllPrintingsForNames
    // ------------------------------------------------------------

    @Test
    void findAllPrintingsForNames_flattensResults() {
        CardEntity e1 = entity();
        CardEntity e2 = entity();

        when(repo.findAllPrintingsForCardName("sol ring"))
                .thenReturn(List.of(e1));
        when(repo.findAllPrintingsForCardName("lightning bolt"))
                .thenReturn(List.of(e2));

        var result = service.findAllPrintingsForNames(List.of("sol ring", "lightning bolt"));

        assertThat(result)
                .containsExactly(CardMapper.toDomain(e1), CardMapper.toDomain(e2));
    }

    // ------------------------------------------------------------
    // buildChecklist
    // ------------------------------------------------------------

    @Test
    void buildChecklist_groupsSortsAndFilters() {
        Card c1 = card("Sol Ring", "cmm", "Commander Masters", "memorabilia",
                "2023-08-01", "W", "123", "paper,arena");

        Card c2 = card("Lightning Bolt", "m10", "Magic 2010", "core",
                "2009-07-17", "R", "150", "paper");

        var result = service.buildChecklist(
                List.of(c1, c2),
                List.of("paper")
        );

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.getSetCode()).isEqualTo("m10");
                    assertThat(dto.getCards()).hasSize(1);
                    assertThat(dto.getSetDate()).isEqualTo(LocalDate.parse("2009-07-17"));
                });
    }

    // ------------------------------------------------------------
    // chooseBestPrinting
    // ------------------------------------------------------------

    @Test
    void chooseBestPrinting_prefersMatchingSet() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", null);
        Card c2 = card("Sol Ring", "m10", null, null, null, null, "123", null);

        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring",
                Optional.of("m10"), Optional.empty(), "raw");

        Card result = CardProcessingService.chooseBestPrinting(List.of(c1, c2), line);

        assertThat(result).isEqualTo(c2);
    }

    @Test
    void chooseBestPrinting_prefersMatchingCollectorNumber() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", null);
        Card c2 = card("Sol Ring", "cmm", null, null, null, null, "350", null);

        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring",
                Optional.empty(), Optional.of("350"), "raw");

        Card result = CardProcessingService.chooseBestPrinting(List.of(c1, c2), line);

        assertThat(result).isEqualTo(c2);
    }

    @Test
    void chooseBestPrinting_returnsNullIfNoMatch() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", null);

        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring",
                Optional.of("m10"), Optional.empty(), "raw");

        Card result = CardProcessingService.chooseBestPrinting(List.of(c1), line);

        assertThat(result).isNull();
    }

    // ------------------------------------------------------------
    // buildDecklist
    // ------------------------------------------------------------

    @Test
    void buildDecklist_findsCardsAndSorts() {
        String input = """
                1 Sol Ring (CMM) 123
                2 Lightning Bolt (M10) 150
                """;

        // Repository returns ENTITIES
        CardEntity solEntity = new CardEntity();
        solEntity.setName("Sol Ring");
        solEntity.setSetName("Commander Masters");
        solEntity.setSetType("masters");
        solEntity.setSetCode("cmm");
        solEntity.setCollectorNumber("123");
        solEntity.setTypeLine("Artifact");

        CardEntity boltEntity = new CardEntity();
        boltEntity.setName("Lightning Bolt");
        boltEntity.setSetName("Magic 2010");
        boltEntity.setSetType("core");
        boltEntity.setSetCode("m10");
        boltEntity.setCollectorNumber("150");
        boltEntity.setTypeLine("Instant");

        when(repo.findAllPrintingsForCardName("sol ring"))
                .thenReturn(List.of(solEntity));

        when(repo.findAllPrintingsForCardName("lightning bolt"))
                .thenReturn(List.of(boltEntity));

        // Service converts them to domain Cards
        Tuple<List<CardEntry>, List<String>> result = service.buildDecklist(input);

        assertThat(result._1()).hasSize(2);
        assertThat(result._2()).isEmpty();

        // Validate the domain cards
        List<Card> cards = result._1().stream()
                .map(CardEntry::getCard)
                .toList();

        assertThat(cards)
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt", "Sol Ring");
    }

    @Test
    void buildDecklist_recordsMissingCards() {
        String input = "1 Missing Card";

        when(repo.findAllPrintingsForCardName("missing card"))
                .thenReturn(List.of());

        Tuple<List<CardEntry>, List<String>> result = service.buildDecklist(input);

        assertThat(result._1()).isEmpty();
        assertThat(result._2()).containsExactly("1 Missing Card");
    }
}