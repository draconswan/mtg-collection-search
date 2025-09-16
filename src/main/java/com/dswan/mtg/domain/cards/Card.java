package com.dswan.mtg.domain.cards;

import com.dswan.mtg.util.CardColorComparator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Slf4j
public class Card {
    @Id
    private String id;
    private String oracle_id;
    private String name;
    private String scryfall_uri;
    private String lang;
    private String released_at;
    @Transient
    private CardTypeInfo card_types;
    private String type_line;
    @Transient
    private List<String> color_identity;
    private String color;
    @JsonProperty(value = "set")
    private String set_code;
    private String set_name;
    private String set_type;
    private String rarity;
    private String collector_number;
    @Transient
    private List<String> games;
    private String gamesList;

    public void populateFromJSON() {
        this.card_types = CardTypeInfo.fromTypeLine(this.type_line);
        this.gamesList = String.join(",", games);
        if (!CollectionUtils.isEmpty(color_identity)) {
            List<String> sortedColors = color_identity.stream()
                    .sorted((o1, o2) -> {
                        CardColorComparator cardColorComparator = new CardColorComparator();
                        return cardColorComparator.compare(o1, o2);
                    })
                    .toList();
            this.color = String.join("", sortedColors);
        }
    }

    @PostLoad
    public void onLoad() {
        this.card_types = CardTypeInfo.fromTypeLine(this.type_line);
        if (!StringUtils.isEmpty(this.color)) {
            this.color_identity = Arrays.asList(this.color.split(""));
        }
        this.games = Arrays.asList(gamesList.split(","));
    }
}
