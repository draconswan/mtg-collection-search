package com.dswan.mtg.util;

import java.text.Normalizer;

public class CardNameNormalizer {
    public static String normalizeCardName(String input) {
        if (input == null) {
            return null;
        }

        // Normalize Unicode accents (e.g., é → e)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace curly quotes/apostrophes with straight ones
        normalized = normalized.replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"");

        // Replace dashes with hyphen
        normalized = normalized.replace("–", "-")
                .replace("—", "-");

        // Replace non-breaking spaces with regular space
        normalized = normalized.replace("\u00A0", " ");

        // Remove trademark and copyright symbols
        normalized = normalized.replace("™", "")
                .replace("©", "")
                .replace("®", "");

        // Trim and lowercase
        return normalized.trim().toLowerCase();
    }
}
