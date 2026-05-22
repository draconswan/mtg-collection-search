package com.dswan.mtg.client;

import com.dswan.mtg.dto.BulkDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ScryfallBulkDataWebClientService {
    private final RestClient restClient;

    public BulkDataResponse getBulkDataURLs() {
        String url = "https://api.scryfall.com/bulk-data";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(BulkDataResponse.class);
    }
}
