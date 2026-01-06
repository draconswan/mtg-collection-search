package com.dswan.mtg.client;

import com.dswan.mtg.dto.BulkDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ScryfallBulkDataWebClientService {
    private final RestTemplate restTemplate;

    public BulkDataResponse getBulkDataURLs() {
        String url = "https://api.scryfall.com/bulk-data";
        return restTemplate.getForObject(url, BulkDataResponse.class);
    }

    public String fetchBulkDataFile(String url) {
        return restTemplate.getForObject(url, String.class);
    }
}
