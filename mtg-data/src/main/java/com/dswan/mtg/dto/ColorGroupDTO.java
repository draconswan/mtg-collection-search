package com.dswan.mtg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorGroupDTO {
    private String color; // "White", "Blue", "Black", "Red", "Green", "Colorless", "Multi"
    private List<RarityGroupDTO> rarities;
}
