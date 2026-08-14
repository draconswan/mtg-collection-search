package com.dswan.mtg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class ManaboxCsvRow {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Set code")
    private String setCode;

    @JsonProperty("Collector number")
    private String collectorNumber;

    @JsonProperty("Quantity")
    private Integer quantity;

    @JsonProperty("Scryfall ID")
    private UUID scryfallId;
}
