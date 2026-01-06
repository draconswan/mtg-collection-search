package com.dswan.mtg.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkDataResponse {
    private String object;
    private List<BulkDataItem> data;
}
