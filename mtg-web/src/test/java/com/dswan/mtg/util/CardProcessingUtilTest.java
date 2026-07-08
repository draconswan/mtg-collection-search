package com.dswan.mtg.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CardProcessingUtilTest {

    @DisplayName("cleanName handles various input formats")
    @ParameterizedTest(name = "cleanName(\"{0}\") → \"{1}\"")
    @MethodSource("cleanNameCases")
    void cleanName_handlesVariousInputs(String input, String expected) {
        assertThat(CardProcessingUtil.cleanName(input)).isEqualTo(expected);
    }

    private static Stream<Object[]> cleanNameCases() {
        return Stream.of(
                // (your existing cases unchanged)
                new Object[]{null, ""},
                new Object[]{"", ""},
                new Object[]{"   ", ""},

                new Object[]{"1 Auramancer", "Auramancer"},
                new Object[]{"1x Auramancer", "Auramancer"},
                new Object[]{"3x   Lightning Bolt", "Lightning Bolt"},
                new Object[]{"12X Counterspell", "Counterspell"},

                new Object[]{"SB: 1x Auramancer", "Auramancer"},
                new Object[]{"Sideboard: 2x Negate", "Negate"},
                new Object[]{"Commander: 1x Atraxa, Praetors' Voice", "Atraxa, Praetors' Voice"},
                new Object[]{"Companion: 1x Jegantha, the Wellspring", "Jegantha, the Wellspring"},

                new Object[]{"Auramancer (M21)", "Auramancer"},
                new Object[]{"Auramancer [M21]", "Auramancer"},
                new Object[]{"Lightning Bolt (2XM) 123", "Lightning Bolt"},
                new Object[]{"Lightning Bolt [2XM] 123a", "Lightning Bolt"},

                new Object[]{"Auramancer (M21) 012", "Auramancer"},
                new Object[]{"Auramancer [M21] 12a", "Auramancer"},
                new Object[]{"Auramancer (M21) 123★", "Auramancer"},
                new Object[]{"Auramancer (M21) ABC-123", "Auramancer"},

                new Object[]{"Auramancer 012", "Auramancer 012"},
                new Object[]{"Lightning Bolt 123a", "Lightning Bolt 123a"},

                new Object[]{"Auramancer foil", "Auramancer"},
                new Object[]{"Auramancer Etched", "Auramancer"},
                new Object[]{"Auramancer showcase", "Auramancer"},
                new Object[]{"Auramancer borderless", "Auramancer"},
                new Object[]{"Auramancer extended art", "Auramancer"},
                new Object[]{"Auramancer promo", "Auramancer"},

                new Object[]{"Auramancer (Promo)", "Auramancer"},
                new Object[]{"Lightning Bolt (Judge Gift)", "Lightning Bolt"},

                new Object[]{"  Auramancer    foil   ", "Auramancer"},

                new Object[]{"Valakut Awakening // Valakut Stoneforge",
                        "Valakut Awakening // Valakut Stoneforge"},
                new Object[]{"Fire // Ice (MMA) 123", "Fire // Ice"},

                new Object[]{"Sol Ring (CMM) 123-EN-foil", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) EN-123-foil", "Sol Ring"},

                new Object[]{"Sol Ring (CMM) 123★", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123†", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123‡", "Sol Ring"},

                new Object[]{"Sol Ring (CMM) 123 *F*", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123 F", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123 *E*", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123 *EA*", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123 *B*", "Sol Ring"},

                new Object[]{"Teferi's Protection (STA) 74e *F*", "Teferi's Protection"},
                new Object[]{"Emeria, the Sky Ruin (PLIST) 287 *F*", "Emeria, the Sky Ruin"}
        );
    }

    @DisplayName("extractCollectorNumber handles various input formats")
    @ParameterizedTest(name = "extractCollectorNumber(\"{0}\") → \"{1}\"")
    @MethodSource("extractCollectorNumberCases")
    void extractCollectorNumber_handlesVariousInputs(String input, Optional<String> expected) {
        assertThat(CardProcessingUtil.extractCollectorNumber(input)).isEqualTo(expected);
    }

    private static Stream<Object[]> extractCollectorNumberCases() {
        return Stream.of(
                new Object[]{null, Optional.empty()},
                new Object[]{"", Optional.empty()},
                new Object[]{"   ", Optional.empty()},

                new Object[]{"1 Auramancer", Optional.empty()},
                new Object[]{"Auramancer 012", Optional.empty()},

                new Object[]{"Lightning Bolt (2XM) 123", Optional.of("123")},
                new Object[]{"Lightning Bolt [2XM] 123a", Optional.of("123a")},

                new Object[]{"Teferi's Protection (STA) 74e *F*", Optional.of("74e")},
                new Object[]{"Card Name (SET) 102b", Optional.of("102b")},

                new Object[]{"Sol Ring (CMM) 123★", Optional.of("123★")},
                new Object[]{"Sol Ring (CMM) 123†", Optional.of("123†")},
                new Object[]{"Sol Ring (CMM) 123‡", Optional.of("123‡")},

                new Object[]{"Sol Ring (CMM) 123-EN-foil", Optional.empty()},
                new Object[]{"Sol Ring (CMM) EN-123-foil", Optional.empty()},

                new Object[]{"Card Name (SET) ABC-123", Optional.of("ABC-123")}
        );
    }

    @DisplayName("notSectionHeader correctly identifies section headers")
    @Test
    void notSectionHeader_tests() {
        assertThat(CardProcessingUtil.notSectionHeader("Sideboard")).isFalse();
        assertThat(CardProcessingUtil.notSectionHeader("SB: stuff")).isFalse();
        assertThat(CardProcessingUtil.notSectionHeader("Commander")).isFalse();
        assertThat(CardProcessingUtil.notSectionHeader("Companion")).isFalse();
        assertThat(CardProcessingUtil.notSectionHeader("Tokens")).isFalse();

        assertThat(CardProcessingUtil.notSectionHeader("Creatures")).isTrue();
        assertThat(CardProcessingUtil.notSectionHeader("Artifacts")).isTrue();
        assertThat(CardProcessingUtil.notSectionHeader("Random text")).isTrue();
    }

    @DisplayName("calculateCMC handles mana symbols correctly")
    @Test
    void calculateCMC_tests() {
        assertThat(CardProcessingUtil.calculateCMC(null)).isEqualTo(0);
        assertThat(CardProcessingUtil.calculateCMC("")).isEqualTo(0);

        assertThat(CardProcessingUtil.calculateCMC("{1}{W}")).isEqualTo(2);
        assertThat(CardProcessingUtil.calculateCMC("{3}{U}{U}")).isEqualTo(5);
        assertThat(CardProcessingUtil.calculateCMC("{X}{X}{2}")).isEqualTo(2);
        assertThat(CardProcessingUtil.calculateCMC("{2/U}{2/U}")).isEqualTo(4);
        assertThat(CardProcessingUtil.calculateCMC("{G/U}{G/U}")).isEqualTo(2);
        assertThat(CardProcessingUtil.calculateCMC("{C}{C}{C}")).isEqualTo(3);
        assertThat(CardProcessingUtil.calculateCMC("{S}{S}")).isEqualTo(2);
    }

    @DisplayName("extractQuantity handles numeric prefixes")
    @Test
    void extractQuantity_tests() {
        assertThat(CardProcessingUtil.extractQuantity("1 Auramancer")).isEqualTo(1);
        assertThat(CardProcessingUtil.extractQuantity("3x Lightning Bolt")).isEqualTo(3);
        assertThat(CardProcessingUtil.extractQuantity("12X Counterspell")).isEqualTo(12);

        // Default quantity
        assertThat(CardProcessingUtil.extractQuantity("Auramancer")).isEqualTo(1);
        assertThat(CardProcessingUtil.extractQuantity("x Auramancer")).isEqualTo(1);
    }

    @DisplayName("extractSet handles set code detection and normalization")
    @Test
    void extractSet_tests() {
        assertThat(CardProcessingUtil.extractSet("Auramancer (M21)")).contains("m21");
        assertThat(CardProcessingUtil.extractSet("Card [2XM]")).contains("2xm");
        assertThat(CardProcessingUtil.extractSet("Emeria (PLIST)")).contains("plst");
        assertThat(CardProcessingUtil.extractSet("Card (PHED)")).contains("slx");
        assertThat(CardProcessingUtil.extractSet("Auramancer")).isEmpty();
    }
}