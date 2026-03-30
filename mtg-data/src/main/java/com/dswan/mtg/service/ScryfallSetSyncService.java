package com.dswan.mtg.service;

import com.dswan.mtg.domain.entity.SetEntity;
import com.dswan.mtg.repository.SetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ScryfallSetSyncService {

    private final SetRepository setRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void syncSets() {

        String url = "https://api.scryfall.com/sets";
        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        JsonNode data = root.get("data");

        for (JsonNode set : data) {

            String code = set.get("code").asString().toLowerCase();
            String name = set.get("name").asString();
            String releasedAt = set.get("released_at").asString(null);
            String iconUrl = set.get("icon_svg_uri").asString(null);

            // Download SVG (may be null)
            String svg = downloadSvg(iconUrl);

            // Check if set already exists
            Optional<SetEntity> existingOpt = setRepository.findById(code);

            if (existingOpt.isEmpty()) {
                // INSERT
                SetEntity entity = new SetEntity();
                entity.setCode(code);
                entity.setName(name);
                entity.setReleasedAt(releasedAt != null ? LocalDate.parse(releasedAt) : null);
                entity.setIconSvg(svg);

                setRepository.save(entity);
                log.info("Inserted new set: {} ({})", code, name);
                continue;
            }

            // UPDATE (only changed fields)
            SetEntity existing = existingOpt.get();
            boolean changed = false;

            if (!Objects.equals(existing.getName(), name)) {
                existing.setName(name);
                changed = true;
            }

            LocalDate parsedDate = releasedAt != null ? LocalDate.parse(releasedAt) : null;
            if (!Objects.equals(existing.getReleasedAt(), parsedDate)) {
                existing.setReleasedAt(parsedDate);
                changed = true;
            }

            if (!Objects.equals(existing.getIconSvg(), svg)) {
                existing.setIconSvg(svg);
                changed = true;
            }

            if (changed) {
                setRepository.save(existing);
                log.info("Updated set: {} ({})", code, name);
            } else {
                log.debug("Unchanged set: {} ({})", code, name);
            }
        }
    }

    private String downloadSvg(String iconUrl) {
        if (iconUrl == null) {
            return null;
        }
        try {
            return restTemplate.getForObject(iconUrl, String.class);
        } catch (Exception e) {
            log.warn("Failed to download SVG from {}", iconUrl);
            return null;
        }
    }
}