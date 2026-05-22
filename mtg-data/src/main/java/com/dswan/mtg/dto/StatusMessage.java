package com.dswan.mtg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusMessage {
    private String message;
    private int count;

    public StatusMessage(String message) {
        this.message = message;
        this.count = 0;
    }
}
