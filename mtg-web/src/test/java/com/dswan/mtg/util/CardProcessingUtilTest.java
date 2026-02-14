package com.dswan.mtg.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;

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
                // Null and trivial
                new Object[]{null, ""},
                new Object[]{"", ""},
                new Object[]{"   ", ""},

                // Quantity stripping
                new Object[]{"1 Auramancer", "Auramancer"},
                new Object[]{"1x Auramancer", "Auramancer"},
                new Object[]{"3x   Lightning Bolt", "Lightning Bolt"},
                new Object[]{"12X Counterspell", "Counterspell"},

                // Section prefixes
                new Object[]{"SB: 1x Auramancer", "Auramancer"},
                new Object[]{"Sideboard: 2x Negate", "Negate"},
                new Object[]{"Commander: 1x Atraxa, Praetors' Voice", "Atraxa, Praetors' Voice"},
                new Object[]{"Companion: 1x Jegantha, the Wellspring", "Jegantha, the Wellspring"},

                // Set codes
                new Object[]{"Auramancer (M21)", "Auramancer"},
                new Object[]{"Auramancer [M21]", "Auramancer"},
                new Object[]{"Lightning Bolt (2XM) 123", "Lightning Bolt"},
                new Object[]{"Lightning Bolt [2XM] 123a", "Lightning Bolt"},

                // Collector numbers (after set code)
                new Object[]{"Auramancer (M21) 012", "Auramancer"},
                new Object[]{"Auramancer [M21] 12a", "Auramancer"},
                new Object[]{"Auramancer (M21) 123★", "Auramancer"},
                new Object[]{"Auramancer (M21) ABC-123", "Auramancer"},

                // Collector numbers WITHOUT set code (should NOT be removed)
                new Object[]{"Auramancer 012", "Auramancer 012"},
                new Object[]{"Lightning Bolt 123a", "Lightning Bolt 123a"},

                // Promo/variant keywords
                new Object[]{"Auramancer foil", "Auramancer"},
                new Object[]{"Auramancer Etched", "Auramancer"},
                new Object[]{"Auramancer showcase", "Auramancer"},
                new Object[]{"Auramancer borderless", "Auramancer"},
                new Object[]{"Auramancer extended art", "Auramancer"},
                new Object[]{"Auramancer promo", "Auramancer"},

                // Parentheses cleanup
                new Object[]{"Auramancer (Promo)", "Auramancer"},
                new Object[]{"Lightning Bolt (Judge Gift)", "Lightning Bolt"},

                // Whitespace collapsing
                new Object[]{"  Auramancer    foil   ", "Auramancer"},

                // MDFC / split cards
                new Object[]{"Valakut Awakening // Valakut Stoneforge",
                        "Valakut Awakening // Valakut Stoneforge"},
                new Object[]{"Fire // Ice (MMA) 123", "Fire // Ice"},

                // Moxfield multi-dash collector numbers
                new Object[]{"Sol Ring (CMM) 123-EN-foil", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) EN-123-foil", "Sol Ring"},

                // Unicode suffixes
                new Object[]{"Sol Ring (CMM) 123★", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123†", "Sol Ring"},
                new Object[]{"Sol Ring (CMM) 123‡", "Sol Ring"}
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
                // Null and trivial
                new Object[]{null, Optional.empty()},
                new Object[]{"", Optional.empty()},
                new Object[]{"   ", Optional.empty()},

                // Quantity stripping
                new Object[]{"1 Auramancer", Optional.empty()},
                new Object[]{"1x Auramancer", Optional.empty()},
                new Object[]{"3x   Lightning Bolt", Optional.empty()},
                new Object[]{"12X Counterspell", Optional.empty()},

                // Section prefixes
                new Object[]{"SB: 1x Auramancer", Optional.empty()},
                new Object[]{"Sideboard: 2x Negate", Optional.empty()},
                new Object[]{"Commander: 1x Atraxa, Praetors' Voice", Optional.empty()},
                new Object[]{"Companion: 1x Jegantha, the Wellspring", Optional.empty()},

                // Set codes
                new Object[]{"Auramancer (M21)", Optional.empty()},
                new Object[]{"Auramancer [M21]", Optional.empty()},
                new Object[]{"Lightning Bolt (2XM) 123", Optional.of("123")},
                new Object[]{"Lightning Bolt [2XM] 123a", Optional.of("123a")},

                // Collector numbers (after set code)
                new Object[]{"Auramancer (M21) 012", Optional.of("012")},
                new Object[]{"Auramancer [M21] 12a", Optional.of("12a")},
                new Object[]{"Auramancer (M21) 123★", Optional.of("123★")},
                new Object[]{"Auramancer (M21) ABC-123", Optional.of("ABC-123")},

                // Collector numbers WITHOUT set code (should NOT be removed)
                new Object[]{"Auramancer 012", Optional.empty()},
                new Object[]{"Lightning Bolt 123a", Optional.empty()},

                // Promo/variant keywords
                new Object[]{"Auramancer foil", Optional.empty()},
                new Object[]{"Auramancer Etched", Optional.empty()},
                new Object[]{"Auramancer showcase", Optional.empty()},
                new Object[]{"Auramancer borderless", Optional.empty()},
                new Object[]{"Auramancer extended art", Optional.empty()},
                new Object[]{"Auramancer promo", Optional.empty()},

                // Parentheses cleanup
                new Object[]{"Auramancer (Promo)", Optional.empty()},
                new Object[]{"Lightning Bolt (Judge Gift)", Optional.empty()},

                // Whitespace collapsing
                new Object[]{"  Auramancer    foil   ", Optional.empty()},

                // MDFC / split cards
                new Object[]{"Valakut Awakening // Valakut Stoneforge", Optional.empty()},
                new Object[]{"Fire // Ice (MMA) 123", Optional.of("123")},

                // Moxfield multi-dash collector numbers
                new Object[]{"Sol Ring (CMM) 123-EN-foil", Optional.empty()},
                new Object[]{"Sol Ring (CMM) EN-123-foil", Optional.empty()},

                // Unicode suffixes
                new Object[]{"Sol Ring (CMM) 123★", Optional.of("123★")},
                new Object[]{"Sol Ring (CMM) 123†", Optional.of("123†")},
                new Object[]{"Sol Ring (CMM) 123‡", Optional.of("123‡")}
        );
    }
}