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
import tools.jackson.databind.ObjectMapper;

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
    private String flavor_name;
    private String scryfall_uri;
    @Transient
    private Images image_uris;
    private String images;
    private String lang;
    private String released_at;
    @Transient
    private CardTypeInfo card_types;
    private String type_line;
    @Transient
    private List<String> color_identity;
    private String color;
    private String mana_cost;
    @JsonProperty(value = "set")
    private String set_code;
    private String set_name;
    private String set_type;
    private String rarity;
    private String collector_number;
    @Transient
    private List<String> games;
    private String gamesList;
    @Transient
    private Prices prices;
    private String pricesList;
    @Transient
    private String castingCostAndIdentity;

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
        if (StringUtils.isNotEmpty(mana_cost)) {
            this.mana_cost = mana_cost.replace("{", "").replace(",", "").replace("}", "");
        }
        if (!CollectionUtils.isEmpty(color_identity) && StringUtils.isNotEmpty(mana_cost)) {
            this.castingCostAndIdentity = mana_cost + " / " + color;
        } else {
            this.castingCostAndIdentity = "";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        this.pricesList = objectMapper.writeValueAsString(prices);
        this.images = objectMapper.writeValueAsString(image_uris);
    }

    @PostLoad
    public void onLoad() {
        this.card_types = CardTypeInfo.fromTypeLine(this.type_line);
        if (!StringUtils.isEmpty(this.color)) {
            this.color_identity = Arrays.asList(this.color.split(""));
        }
        this.games = Arrays.asList(gamesList.split(","));
        ObjectMapper objectMapper = new ObjectMapper();
        if (!StringUtils.isEmpty(this.pricesList)) {
            this.prices = objectMapper.readValue(pricesList, Prices.class);
        }
        if (!StringUtils.isEmpty(this.images)) {
            this.image_uris = objectMapper.readValue(images, Images.class);
        }
    }
}
