package com.dswan.mtg.domain.model;

import lombok.Data;

@Data
public class CardStateForm {
    private String cardId;
    private int quantity;
    private boolean checked;
}