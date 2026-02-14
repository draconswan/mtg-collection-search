package com.dswan.mtg.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "card", schema = "public")
@Data
public class CardEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "oracle_id")
    private String oracleId;

    @Column(name = "\"name\"")
    private String name;

    @Column(name = "flavor_name")
    private String flavorName;

    @Column(name = "printed_name")
    private String printedName;

    @Column(name = "scryfall_uri")
    private String scryfallUri;

    @Column(name = "images")
    private String images;

    @Column(name = "lang")
    private String lang;

    @Column(name = "released_at")
    private LocalDate releasedAt;

    @Column(name = "type_line")
    private String typeLine;

    @Column(name = "color")
    private String color;

    @Column(name = "mana_cost")
    private String manaCost;

    @Column(name = "set_code")
    private String setCode;

    @Column(name = "set_name")
    private String setName;

    @Column(name = "set_type")
    private String setType;

    @Column(name = "rarity")
    private String rarity;

    @Column(name = "collector_number")
    private String collectorNumber;

    @Column(name = "games_list")
    private String gamesList;

    @Column(name = "prices_list")
    private String pricesList;
}