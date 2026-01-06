package com.dswan.mtg.domain;

import java.util.Optional;

public record ParsedCardLine(
        int quantity,
        String name,
        Optional<String> set,
        Optional<String> collectorNumber
) {
}