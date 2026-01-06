package com.dswan.mtg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class UpdateResult {
    private boolean success;
    private int cardsProcessed;
    private String errorMessage;
}