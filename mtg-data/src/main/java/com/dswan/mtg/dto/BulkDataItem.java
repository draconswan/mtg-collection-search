package com.dswan.mtg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class BulkDataItem {
    private String id;
    private String type;
    private String name;
    @JsonProperty("download_uri")
    private String downloadUri;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    private String description;
}