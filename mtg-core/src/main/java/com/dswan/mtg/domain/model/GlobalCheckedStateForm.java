package com.dswan.mtg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class GlobalCheckedStateForm {
    private List<DeckCardCheckState> cards;
}
