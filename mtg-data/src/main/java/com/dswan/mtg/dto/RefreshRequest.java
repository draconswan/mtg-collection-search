package com.dswan.mtg.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private boolean force;
    private boolean updateSets;
}
