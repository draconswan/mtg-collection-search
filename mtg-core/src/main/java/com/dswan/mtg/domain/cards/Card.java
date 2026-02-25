package com.dswan.mtg.domain.cards;

import com.dswan.mtg.util.CardColorComparator;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Card {
    //Fields from the Scryfall JSON
    private String id;
    private String oracleId;
    private String name;
    private String flavorName;
    private String printedName;
    private String lang;
    private String releasedAt;
    private String scryfallUri;
    private List<CardFace> cardFaces;
    private Images imageUris;
    private String manaCost;
    private String typeLine;
    private List<String> colorIdentity;
    private List<String> games;
    private String set;
    private String setName;
    private String setType;
    private String collectorNumber;
    private String rarity;
    private Prices prices;

    //Database fields
    private String images;
    private String color;
    private String gamesList;
    private String pricesList;

    //Derived Fields
    @Transient
    private CardTypeInfo cardTypes;
    @Transient
    private Integer quantity = 0;
    @Transient
    private boolean checked = false;
    @Transient
    private String location = "mainboard";
    @Transient
    private String castingCostAndIdentity;

    public String getDisplayName() {
        if (!lang.equals("en")) {
            return name;
        }
        if (StringUtils.isNotBlank(printedName)) {
            return printedName;
        }
        if (StringUtils.isNotBlank(flavorName)) {
            return flavorName + " (" + name + ")";
        }
        return name;
    }

    public void populateFromJSON() {
        this.cardTypes = CardTypeInfo.fromTypeLine(this.typeLine);
        if (games != null) {
            this.gamesList = String.join(",", games);
        }
        if (!CollectionUtils.isEmpty(colorIdentity)) {
            this.color = colorIdentity.stream()
                    .sorted(new CardColorComparator())
                    .reduce("", (a, b) -> a + b);
        } else {
            this.color = "";
        }
        if (StringUtils.isNotEmpty(manaCost)) {
            this.manaCost = manaCost.replace(" ", "");
        } else if (CollectionUtils.isNotEmpty(this.cardFaces)) {
            CardFace first = this.cardFaces.getFirst();
            if (StringUtils.isNotEmpty(first.getManaCost())) {
                this.manaCost = first.getManaCost().replace(" ", "");
            } else {
                this.manaCost = "";
            }
        } else {
            this.manaCost = "";
        }
        if (!CollectionUtils.isEmpty(colorIdentity) && StringUtils.isNotEmpty(manaCost)) {
            this.castingCostAndIdentity = manaCost + " / " + color;
        } else {
            this.castingCostAndIdentity = "";
        }
        if (this.oracleId == null && CollectionUtils.isNotEmpty(this.cardFaces)) {
            this.oracleId = this.cardFaces.getFirst().getOracleId();
        }
        if (imageUris == null && CollectionUtils.isNotEmpty(this.cardFaces)) {
            this.imageUris = this.cardFaces.getFirst().getImageUris();
        }
        if (this.typeLine == null && CollectionUtils.isNotEmpty(this.cardFaces)) {
            this.typeLine = this.cardFaces.getFirst().getTypeLine();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.pricesList = mapper.writeValueAsString(prices);
            this.images = mapper.writeValueAsString(imageUris);
        } catch (Exception e) {
            log.error("Error serializing JSON fields", e);
        }
    }

    public void hydrateFromEntity() {
        this.cardTypes = CardTypeInfo.fromTypeLine(this.typeLine);
        if (StringUtils.isNotEmpty(this.color)) {
            this.colorIdentity = Arrays.asList(this.color.split(""));
        }
        if (StringUtils.isNotEmpty(this.gamesList)) {
            this.games = Arrays.asList(gamesList.split(","));
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (StringUtils.isNotEmpty(this.pricesList)) {
                this.prices = mapper.readValue(pricesList, Prices.class);
            }
            if (StringUtils.isNotEmpty(this.images)) {
                this.imageUris = mapper.readValue(images, Images.class);
            }
        } catch (Exception e) {
            log.error("Error parsing JSON fields", e);
        }
    }
}