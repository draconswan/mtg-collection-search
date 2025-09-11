package com.dswan.mtg.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    @JsonProperty(value = "set")
    private String set_code;
    private String set_name;
    private String set_type;
    private String rarity;
    private String collector_number;
}
