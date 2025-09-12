package com.dswan.mtg.dto;

import lombok.Data;

import java.util.List;

@Data
public class CardListRequest {
    private List<String> cardNames;
}
