package com.dswan.mtg.service;

import com.dswan.mtg.domain.entity.SetEntity;
import com.dswan.mtg.repository.SetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScryfallSetSyncService {

    private final SetRepository setRepository;
    private final RestClient restClient;

    private static final String SCRYFALL_SETS_URL = "https://api.scryfall.com/sets";

    public void syncSets() {
        log.info("Starting Scryfall set sync");
        String nextPage = SCRYFALL_SETS_URL;
        while (nextPage != null) {
            JsonNode root = fetchJson(nextPage);
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                log.error("Unexpected Scryfall response: {}", root);
                return;
            }
            data.forEach(this::processSet);
            nextPage = root.path("has_more").asBoolean(false)
                    ? root.path("next_page").asText(null)
                    : null;
        }
        log.info("Completed Scryfall set sync");
    }

    private void processSet(JsonNode set) {
        String code = set.path("code").asText().toLowerCase();
        String name = set.path("name").asText();

        String releasedAtRaw = set.path("released_at").asText(null);
        LocalDate releasedAt = (releasedAtRaw != null && !releasedAtRaw.isBlank())
                ? LocalDate.parse(releasedAtRaw)
                : null;
        String iconUrl = set.path("icon_svg_uri").asText(null);
        String svgXml = downloadSvgXml(iconUrl);
        Optional<SetEntity> existingOpt = setRepository.findById(code);
        if (existingOpt.isEmpty()) {
            insertSet(code, name, releasedAt, svgXml);
            return;
        }
        updateSet(existingOpt.get(), name, releasedAt, svgXml);
    }

    private void insertSet(String code, String name, LocalDate releasedAt, String svgXml) {
        SetEntity entity = new SetEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setReleasedAt(releasedAt);
        entity.setIconSvg(svgXml);
        setRepository.save(entity);
        log.info("Inserted new set: {} ({})", code, name);
    }

    private void updateSet(SetEntity existing, String name, LocalDate releasedAt, String svgXml) {
        boolean changed = false;
        if (!equals(existing.getName(), name)) {
            existing.setName(name);
            changed = true;
        }
        if (!equals(existing.getReleasedAt(), releasedAt)) {
            existing.setReleasedAt(releasedAt);
            changed = true;
        }
        if (!equals(existing.getIconSvg(), svgXml)) {
            existing.setIconSvg(svgXml);
            changed = true;
        }
        if (changed) {
            setRepository.save(existing);
            log.info("Updated set: {} ({})", existing.getCode(), name);
        } else {
            log.debug("Unchanged set: {} ({})", existing.getCode(), name);
        }
    }

    private boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    private JsonNode fetchJson(String url) {
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.warn("Failed to fetch Scryfall JSON from {}: {}", url, e.getMessage());
            throw e;
        }
    }

    private String downloadSvgXml(String url) {
        if (url == null) {
            return null;
        }

        try {
            return restClient.get()
                    .uri(url)
                    .header("Accept", "image/svg+xml")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("Failed to download SVG XML from {}: {}", url, e.getMessage());
            return null;
        }
    }
}