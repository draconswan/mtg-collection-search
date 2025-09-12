package com.dswan.mtg.dto;

import com.dswan.mtg.domain.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardSetDTO {
    private String setCode;
    private String setName;
    private String setType;
    private LocalDate setDate;
    private List<Card> cards;
    private List<String> gameTypes;
}