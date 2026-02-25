package com.dswan.mtg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "mtg")
public record MTGProperties(
        Ingest ingest,
        Api api
) {
    public record Ingest(List<String> languages) {}
    public record Api(String refreshEndpoint) {}
}