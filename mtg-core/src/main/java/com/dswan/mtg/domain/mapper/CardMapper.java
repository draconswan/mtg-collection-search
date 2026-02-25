package com.dswan.mtg.domain.mapper;

import com.dswan.mtg.domain.cards.Card;
import com.dswan.mtg.domain.entity.CardEntity;

import java.util.UUID;

public class CardMapper {

    public static Card toDomain(CardEntity e) {
        if (e == null) {
            return null;
        }
        Card c = new Card();
        c.setId(e.getId().toString());
        c.setOracleId(e.getOracleId());
        c.setName(e.getName());
        c.setFlavorName(e.getFlavorName());
        c.setPrintedName(e.getPrintedName());
        c.setScryfallUri(e.getScryfallUri());
        c.setImages(e.getImages());
        c.setLang(e.getLang());
        c.setReleasedAt(e.getReleasedAt() != null ? e.getReleasedAt().toString() : null);
        c.setTypeLine(e.getTypeLine());
        c.setColor(e.getColor());
        c.setManaCost(e.getManaCost());
        c.setSet(e.getSetCode());
        c.setSetName(e.getSetName());
        c.setSetType(e.getSetType());
        c.setRarity(e.getRarity());
        c.setCollectorNumber(e.getCollectorNumber());
        c.setGamesList(e.getGamesList());
        c.setPricesList(e.getPricesList());
        c.hydrateFromEntity();
        return c;
    }

    public static CardEntity toEntity(Card c) {
        if (c == null) {
            return null;
        }
        c.populateFromJSON();
        CardEntity e = new CardEntity();
        e.setId(UUID.fromString(c.getId()));
        e.setOracleId(c.getOracleId());
        e.setName(c.getName());
        e.setFlavorName(c.getFlavorName());
        e.setPrintedName(c.getPrintedName());
        e.setScryfallUri(c.getScryfallUri());
        e.setImages(c.getImages());
        e.setLang(c.getLang());
        e.setReleasedAt(c.getReleasedAt() != null ? java.time.LocalDate.parse(c.getReleasedAt()) : null);
        e.setTypeLine(c.getTypeLine());
        e.setColor(c.getColor() == null ? "" : c.getColor());
        e.setManaCost(c.getManaCost());
        e.setSetCode(c.getSet());
        e.setSetName(c.getSetName());
        e.setSetType(c.getSetType());
        e.setRarity(c.getRarity());
        e.setCollectorNumber(c.getCollectorNumber());
        e.setGamesList(c.getGamesList());
        e.setPricesList(c.getPricesList());
        return e;
    }
}