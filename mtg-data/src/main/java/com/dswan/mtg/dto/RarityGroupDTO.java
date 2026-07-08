package com.dswan.mtg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RarityGroupDTO {
    private String rarity; // "Mythic", "Rare", "Uncommon", "Common"
    private List<CmcGroupDTO> cmcs;
}
