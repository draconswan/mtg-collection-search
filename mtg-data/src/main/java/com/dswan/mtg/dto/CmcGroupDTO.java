package com.dswan.mtg.dto;

import com.dswan.mtg.domain.cards.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CmcGroupDTO {
    private int cmc;
    private List<Card> cards;
}
