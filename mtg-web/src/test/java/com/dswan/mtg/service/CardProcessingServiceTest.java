package com.dswan.mtg.service;

import com.dswan.mtg.domain.ParsedCardLine;
import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.cards.CardEntry;
import com.dswan.mtg.domain.entity.CardEntity;
import com.dswan.mtg.domain.mapper.CardMapper;
import com.dswan.mtg.domain.user.CollectionSortType;
import com.dswan.mtg.dto.CmcGroupDTO;
import com.dswan.mtg.dto.ColorGroupDTO;
import com.dswan.mtg.dto.RarityGroupDTO;
import com.dswan.mtg.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.util.Tuple;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dswan.mtg.util.CardProcessingUtil.calculateCMC;
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
            String rarity,
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
        c.setRarity(rarity);
        c.setGamesList(gamesList);
        return c;
    }

    private CardEntity entity() {
        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(UUID.randomUUID());
        return cardEntity;
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
    // buildChecklist – SET mode
    // ------------------------------------------------------------

    @Test
    void buildChecklist_groupsSortsAndFilters() {
        Card c1 = card("Sol Ring", "cmm", "Commander Masters", "memorabilia",
                "2023-08-01", "W", "123", "uncommon", "paper,arena");

        Card c2 = card("Lightning Bolt", "m10", "Magic 2010", "core",
                "2009-07-17", "R", "150", "common", "paper");

        var result = service.buildChecklist(
                List.of(c1, c2),
                List.of("paper"),
                CollectionSortType.SET
        );

        assertThat(result)
                .isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<?> list = (List<?>) result;

        assertThat(list)
                .hasSize(1)
                .first()
                .satisfies(dtoObj -> {
                    var dto = (com.dswan.mtg.dto.CardSetDTO) dtoObj;
                    assertThat(dto.getSetCode()).isEqualTo("m10");
                    assertThat(dto.getCards()).hasSize(1);
                    assertThat(dto.getSetDate()).isEqualTo(LocalDate.parse("2009-07-17"));
                });
    }

    @Test
    @DisplayName("buildChecklist dispatches to SET mode")
    void buildChecklist_dispatchesToSet() {
        Card c = card("Sol Ring", "cmm", "Commander Masters", "core",
                "2023-08-01", "W", "123", "uncommon", "paper");
        c.setManaCost("{1}");

        var result = service.buildChecklist(
                List.of(c),
                List.of("paper"),
                CollectionSortType.SET
        );

        assertThat(result).isInstanceOf(List.class);
        assertThat(((List<?>) result)).hasSize(1);
    }

    @Test
    @DisplayName("SET mode filters by game type")
    void setMode_filtersByGameType() {
        Card c1 = card("Sol Ring", "cmm", "Commander Masters", "core",
                "2023-08-01", "W", "123", "uncommon", "arena");
        Card c2 = card("Lightning Bolt", "m10", "Magic 2010", "core",
                "2009-07-17", "R", "150", "common", "paper");

        var result = service.buildChecklist(
                List.of(c1, c2),
                List.of("paper"),
                CollectionSortType.SET
        );

        @SuppressWarnings("unchecked")
        List<com.dswan.mtg.dto.CardSetDTO> list = (List<com.dswan.mtg.dto.CardSetDTO>) result;

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCards()).extracting(Card::getName)
                .containsExactly("Lightning Bolt");
    }

    // ------------------------------------------------------------
    // buildChecklist – COLOR_RARITY_CMC mode
    // ------------------------------------------------------------

    @Test
    @DisplayName("buildChecklist dispatches to COLOR_RARITY_CMC mode")
    void buildChecklist_dispatchesToColorRarityCmc() {
        Card c = card("Sol Ring", "cmm", "Commander Masters", "core",
                "2023-08-01", "W", "123", "uncommon", "paper");
        c.setManaCost("{1}{W}");

        var result = service.buildChecklist(
                List.of(c),
                List.of("paper"),
                CollectionSortType.COLOR_RARITY_CMC
        );

        assertThat(result).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<ColorGroupDTO> groups = (List<ColorGroupDTO>) result;

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getColor()).isEqualTo("White");
    }

    @Test
    @DisplayName("COLOR_RARITY_CMC groups by color, rarity, and CMC")
    void colorRarityCmc_groupsCorrectly() {
        Card c1 = card("Sol Ring", "cmm", "Commander Masters", "core",
                "2023-08-01", "W", "123", "uncommon", "paper");
        c1.setManaCost("{1}{W}");

        Card c2 = card("Swords to Plowshares", "2xm", "Double Masters", "core",
                "2020-08-07", "W", "22", "uncommon", "paper");
        c2.setManaCost("{W}");

        var result = service.buildChecklist(
                List.of(c1, c2),
                List.of("paper"),
                CollectionSortType.COLOR_RARITY_CMC
        );

        @SuppressWarnings("unchecked")
        List<ColorGroupDTO> groups = (List<ColorGroupDTO>) result;

        assertThat(groups).hasSize(1);
        ColorGroupDTO white = groups.get(0);

        assertThat(white.getColor()).isEqualTo("White");
        assertThat(white.getRarities()).isNotEmpty();

        RarityGroupDTO rarity = white.getRarities().get(0);
        assertThat(rarity.getCmcs()).hasSize(2);
    }

    @Test
    @DisplayName("CMC grouping inside COLOR_RARITY_CMC is correct")
    void buildChecklist_colorRarityCmc_cmcGroupingCorrect() {
        Card c = card("Test Card", "set", "Set Name", "core",
                "2020-01-01", "U", "001", "Rare", "paper");
        c.setManaCost("{2}{U}");

        var result = service.buildChecklist(
                List.of(c),
                List.of("paper"),
                CollectionSortType.COLOR_RARITY_CMC
        );

        @SuppressWarnings("unchecked")
        List<ColorGroupDTO> groups = (List<ColorGroupDTO>) result;

        CmcGroupDTO cmcGroup = groups.get(0)
                .getRarities().get(0)
                .getCmcs().get(0);

        assertThat(cmcGroup.getCmc()).isEqualTo(3);
        assertThat(cmcGroup.getCards()).extracting(Card::getName)
                .containsExactly("Test Card");
    }

    // ------------------------------------------------------------
    // calculateCMC
    // ------------------------------------------------------------

    @Test
    @DisplayName("calculateCMC handles numeric, hybrid, phyrexian, snow, X")
    void calculateCMC_allSymbols() {
        assertThat(calculateCMC("{1}{W}{W}")).isEqualTo(3);
        assertThat(calculateCMC("{X}{G}")).isEqualTo(1);
        assertThat(calculateCMC("{2/U}{2/U}")).isEqualTo(4);
        assertThat(calculateCMC("{W/P}{G/P}")).isEqualTo(2);
        assertThat(calculateCMC("{S}{S}{G}")).isEqualTo(3);
        assertThat(calculateCMC("")).isEqualTo(0);
    }

    // ------------------------------------------------------------
    // chooseBestPrinting
    // ------------------------------------------------------------

    @Test
    void chooseBestPrinting_prefersMatchingSet() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", "uncommon", null);
        Card c2 = card("Sol Ring", "m10", null, null, null, null, "123", "uncommon", null);
        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring", Optional.of("m10"), Optional.empty(), "raw");
        Card result = CardProcessingService.chooseBestPrinting(List.of(c1, c2), line);
        assertThat(result).isEqualTo(c2);
    }

    @Test
    void chooseBestPrinting_prefersMatchingCollectorNumber() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", "uncommon", null);
        Card c2 = card("Sol Ring", "cmm", null, null, null, null, "350", "uncommon", null);
        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring", Optional.empty(), Optional.of("350"), "raw");
        Card result = CardProcessingService.chooseBestPrinting(List.of(c1, c2), line);
        assertThat(result).isEqualTo(c2);
    }

    @Test
    void chooseBestPrinting_returnsNullIfNoMatch() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", "uncommon", null);

        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring",
                Optional.of("m10"), Optional.empty(), "raw");

        Card result = CardProcessingService.chooseBestPrinting(List.of(c1), line);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("chooseBestPrinting prefers English when multiple remain")
    void chooseBestPrinting_prefersEnglish() {
        Card c1 = card("Sol Ring", "cmm", null, null, null, null, "123", "uncommon", null);
        c1.setLang("jp");

        Card c2 = card("Sol Ring", "cmm", null, null, null, null, "123", "uncommon", null);
        c2.setLang("en");

        ParsedCardLine line = new ParsedCardLine(1, "Sol Ring",
                Optional.empty(), Optional.empty(), "raw");

        Card result = CardProcessingService.chooseBestPrinting(List.of(c1, c2), line);

        assertThat(result).isEqualTo(c2);
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

        CardEntity solEntity = new CardEntity();
        solEntity.setId(UUID.randomUUID());
        solEntity.setName("Sol Ring");
        solEntity.setSetName("Commander Masters");
        solEntity.setSetType("masters");
        solEntity.setSetCode("cmm");
        solEntity.setCollectorNumber("123");
        solEntity.setTypeLine("Artifact");

        CardEntity boltEntity = new CardEntity();
        boltEntity.setId(UUID.randomUUID());
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

        Tuple<List<CardEntry>, List<String>> result = service.buildDecklist(input);

        assertThat(result._1()).hasSize(2);
        assertThat(result._2()).isEmpty();

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

    @Test
    @DisplayName("buildDecklist handles empty input")
    void buildDecklist_emptyInput() {
        Tuple<List<CardEntry>, List<String>> result = service.buildDecklist("");

        assertThat(result._1()).isEmpty();
        assertThat(result._2()).isEmpty();
    }

    @Test
    @DisplayName("buildDecklist handles missing set and collector number")
    void buildDecklist_missingSetAndCollector() {
        CardEntity e = new CardEntity();
        e.setId(UUID.randomUUID());
        e.setName("Sol Ring");
        e.setSetCode("cmm");
        e.setSetName("Commander Masters");
        e.setSetType("masters");
        e.setReleasedAt(LocalDate.parse("2023-08-01"));
        e.setCollectorNumber("123");
        e.setTypeLine("Artifact");

        when(repo.findAllPrintingsForCardName("sol ring"))
                .thenReturn(List.of(e));

        Tuple<List<CardEntry>, List<String>> result = service.buildDecklist("1 Sol Ring");

        assertThat(result._1()).hasSize(1);
        assertThat(result._2()).isEmpty();
    }
}
