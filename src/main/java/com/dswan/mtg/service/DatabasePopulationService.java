package com.dswan.mtg.service;

import com.dswan.mtg.client.ScryfallBulkDataWebClientService;
import com.dswan.mtg.domain.DataVersion;
import com.dswan.mtg.domain.Card;
import com.dswan.mtg.dto.BulkDataItem;
import com.dswan.mtg.dto.BulkDataResponse;
import com.dswan.mtg.repository.DataVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabasePopulationService {
    public static final String ALL_CARDS = "All Cards";
    private static final File BULK_FILE = new File("data/scryfall-all-cards.json");

    private final ScryfallBulkDataWebClientService scryfallBulkDataWebClientService;
    private final DataVersionRepository dataVersionRepository;
    private final CardBatchService cardBatchService; // <-- injected batch service
    private final ObjectMapper objectMapper;

    public void checkAndUpdateDatabase(boolean forceUpdate) throws Exception {
        List<DataVersion> all = (List<DataVersion>) dataVersionRepository.findAll();
        log.info("Last Database Update: {}", all);
        LocalDateTime dataNeedsRefresh = LocalDateTime.now().minusDays(1);

        if (forceUpdate || CollectionUtils.isEmpty(all) || all.getFirst().getLast_refresh().isBefore(dataNeedsRefresh)) {
            ObjectReader cardReader = objectMapper.readerFor(Card.class);

            BulkDataResponse bulkDataResponse = scryfallBulkDataWebClientService.getBulkDataURLs();
            log.info("Bulk Data info: {}", bulkDataResponse);

            for (BulkDataItem dataItem : bulkDataResponse.getData()) {
                if (ALL_CARDS.equals(dataItem.getName())) {
                    log.info("Found all cards data item: {}", dataItem);

                    boolean shouldDownload = !BULK_FILE.exists();
                    if (!shouldDownload) {
                        Instant fileUpdated = Instant.ofEpochMilli(BULK_FILE.lastModified());
                        Instant scryfallUpdated = dataItem.getUpdatedAt();
                        if (fileUpdated.isBefore(scryfallUpdated)) {
                            shouldDownload = true;
                            log.info("Local file is older than Scryfall data. Updating...");
                        } else {
                            log.info("Local file is up-to-date. Using cached copy.");
                        }
                    }

                    if (shouldDownload) {
                        log.info("Downloading Scryfall All Cards data...");
                        BULK_FILE.getParentFile().mkdirs();
                        downloadWithRetry(new URL(dataItem.getDownloadUri()), BULK_FILE, 3);
                        BULK_FILE.setLastModified(dataItem.getUpdatedAt().toEpochMilli());
                    }

                    try (InputStream inputStream = Files.newInputStream(BULK_FILE.toPath());
                         JsonParser parser = objectMapper.createParser(inputStream)) {

                        if (parser.nextToken() != JsonToken.START_ARRAY) {
                            throw new IllegalStateException("Expected JSON array at root");
                        }
                        if (parser.nextToken() != JsonToken.START_OBJECT) {
                            throw new IllegalStateException("Expected object inside array");
                        }

                        int count = 0;
                        int batchSize = 1000;
                        List<Card> batch = new ArrayList<>();
                        MappingIterator<Card> it = cardReader.readValues(parser);

                        while (it.hasNextValue()) {
                            Card card = it.nextValue();
                            batch.add(card);

                            if (batch.size() >= batchSize) {
                                cardBatchService.saveBatch(batch); // NEW TRANSACTION
                                count += batch.size();
                                log.info("Processed {} cards...", count);
                                batch.clear();
                            }
                        }

                        if (!batch.isEmpty()) {
                            cardBatchService.saveBatch(batch); // leftover
                            count += batch.size();
                        }

                        log.info("Finished processing {} cards total.", count);
                    }
                }
            }
        }
    }

    private void downloadWithRetry(URL url, File target, int maxRetries) throws Exception {
        int attempt = 0;
        while (true) {
            try (InputStream in = url.openStream()) {
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Downloaded bulk file successfully to {}", target.getAbsolutePath());
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("Failed to download bulk file after {} attempts", attempt, e);
                    throw e;
                }
                long backoff = 10000L * attempt;
                log.warn("Download failed (attempt {}/{}). Retrying in {} ms...", attempt, maxRetries, backoff, e);
                Thread.sleep(backoff);
            }
        }
    }
}