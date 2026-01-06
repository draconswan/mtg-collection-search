package com.dswan.mtg.domain.key;

import lombok.Data;

import java.io.Serializable;

@Data
public class CardSetKey implements Serializable {
    private String card_id;
    private String set_code;
}
