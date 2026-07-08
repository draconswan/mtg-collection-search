package com.dswan.mtg.dto;

import com.dswan.mtg.domain.cards.Images;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UncheckedCardDTO {
    private UUID deckId;
    private String deckName;
    private UUID cardId;
    private String oracleId;
    private String scryfallUri;
    private String images;
    private String cardName;
    private String printedName;
    private String flavorName;
    private String lang;
    private String setCode;
    private String setName;
    private LocalDate releasedAt;
    private String typeLine;
    private String manaCost;
    private Long quantity;

    //Derived Fields
    @Transient
    private Images imageUris;

    public String displayName() {
        if (lang != null && !lang.equals("en")) {
            return cardName;
        }
        if (printedName != null && !printedName.isBlank()) {
            return printedName;
        }
        if (flavorName != null && !flavorName.isBlank()) {
            return flavorName + " (" + cardName + ")";
        }
        return cardName;
    }

    public void hydrateFromEntity() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (StringUtils.isNotEmpty(this.images)) {
                this.imageUris = mapper.readValue(images, Images.class);
            }
        } catch (Exception e) {
            log.error("Error parsing JSON fields", e);
        }
    }
}