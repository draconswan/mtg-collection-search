package com.dswan.mtg.util;

import io.micrometer.common.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class CardProcessingUtil {
    private static final List<String> SECTION_HEADERS = List.of(
            "sideboard", "sb:", "commander", "companion",
            "maybeboard", "maybe:", "tokens", "custom", "categories"
    );

    private static final Map<String, String> SET_CODE_NORMALIZATION = Map.of(
            "PLIST", "plst"   // Moxfield → Scryfall mismatch
    );


    public static boolean notSectionHeader(String line) {
        String lower = line.toLowerCase();
        return SECTION_HEADERS.stream().noneMatch(lower::startsWith);
    }

    public static int extractQuantity(String line) {
        String[] parts = line.split("\\s+", 2);
        if (parts[0].matches("\\d+")) return Integer.parseInt(parts[0]);
        if (parts[0].matches("\\d+[xX]")) return Integer.parseInt(parts[0].replace("x", ""));
        return 1;
    }

    public static Optional<String> extractSet(String line) {
        var matcher = Pattern.compile("[\\[(]([A-Za-z0-9]{2,5})[\\])]")
                .matcher(line);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String raw = matcher.group(1).toUpperCase(); // Moxfield always uppercase
        String normalized = SET_CODE_NORMALIZATION.getOrDefault(raw, raw.toLowerCase());
        return Optional.of(normalized);
    }


    public static Optional<String> extractCollectorNumber(String line) {
        if (StringUtils.isEmpty(line)) {
            return Optional.empty();
        }
        var setMatcher = Pattern.compile("[\\[(][A-Za-z0-9]{2,5}[\\])]\\s*").matcher(line);
        if (!setMatcher.find()) {
            return Optional.empty();
        }
        String afterSet = line.substring(setMatcher.end());
        // Capturing group added
        var pattern = Pattern.compile("^[\\s]*([A-Za-z0-9-]*\\d+[A-Za-z0-9-★†‡]*)");
        var matcher = pattern.matcher(afterSet);
        if (matcher.find()) {
            String token = matcher.group(1);
            // Reject multi-dash tokens
            if (token.chars().filter(ch -> ch == '-').count() > 1) {
                return Optional.empty();
            }
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public static String cleanName(String line) {
        if (line == null) {
            return "";
        }
        // Remove section prefixes
        line = line.replaceAll("(?i)^sb:\\s*", "");
        line = line.replaceAll("(?i)^sideboard:\\s*", "");
        line = line.replaceAll("(?i)^commander:\\s*", "");
        line = line.replaceAll("(?i)^companion:\\s*", "");
        // Remove quantity
        line = line.replaceAll("^\\d+[xX]?\\s+", "");
        // Detect (SET) or [SET]
        var setPattern = Pattern.compile("[\\[(][A-Za-z0-9]{2,5}[\\])]\\s*");
        var matcher = setPattern.matcher(line);
        String beforeSet = line;
        String afterSet = "";
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            beforeSet = line.substring(0, start);
            afterSet = line.substring(end);
        }
        // Remove collector numbers and suffixes ONLY after a set code
        afterSet = afterSet.replaceAll("^[\\s]*(?:[A-Za-z0-9-]*\\d+[A-Za-z0-9-★†‡]*)", "");
        // Remove any Moxfield variant markers (F, *F*, E, *E*, EA, *EA*, etc.) ONLY after the collector number
        afterSet = afterSet.replaceAll("\\s*\\*?[A-Za-z]{1,3}\\*?\\s*", " ");
        // Reassemble
        line = beforeSet + afterSet;
        // Remove promo/variant keywords
        line = line.replaceAll("(?i)\\b(foil|etched|retro|showcase|borderless|extended art|promo)\\b", "");
        // Remove any remaining parentheses
        line = line.replaceAll("\\(.*?\\)", "");
        // Collapse whitespace
        line = line.replaceAll("\\s{2,}", " ");
        return line.trim();
    }
}
