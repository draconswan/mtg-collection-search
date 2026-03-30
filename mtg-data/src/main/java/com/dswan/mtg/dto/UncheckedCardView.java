package com.dswan.mtg.dto;

import java.util.List;

public record UncheckedCardView(
        UncheckedCardDTO base,
        List<String> deckColors
) {}