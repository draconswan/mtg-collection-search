package com.dswan.mtg.domain.cards;

import java.util.List;
import java.util.Map;

public class DeckFormats {
    public static final Map<String, List<String>> FORMATS = Map.of(
            "Paper Formats", List.of(
                    "Standard", "Pioneer", "Modern", "Legacy", "Vintage",
                    "Commander", "Oathbreaker", "Pauper", "Premodern"
            ),
            "Digital Formats", List.of(
                    "Historic", "Timeless", "Alchemy"
            ),
            "Casual / Special", List.of(
                    "Brawl", "Gladiator", "Pauper Commander",
                    "Duel", "Penny Dreadful"
            )
    );
}