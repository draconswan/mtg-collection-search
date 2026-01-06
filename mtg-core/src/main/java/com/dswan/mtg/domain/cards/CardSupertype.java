package com.dswan.mtg.domain.cards;

public enum CardSupertype {
    BASIC,
    LEGENDARY,
    ONGOING,
    SNOW,
    WORLD,
    OTHER;

    public static CardSupertype fromString(String value){
        try{
            return CardSupertype.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e){
            return OTHER;
        }
    }
}
