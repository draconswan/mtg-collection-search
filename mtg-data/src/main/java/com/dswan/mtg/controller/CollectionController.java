//package com.dswan.mtg.controller;
//
//import com.dswan.mtg.domain.cards.Card;
//import com.dswan.mtg.dto.ManaboxCsvRow;
//import com.dswan.mtg.service.UserCollectionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//import tools.jackson.databind.MappingIterator;
//
//import java.io.IOException;
//import java.security.Principal;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/collection")
//@RequiredArgsConstructor
//public class CollectionController {
//
//    private UserCollectionService userCollectionService;
//
//    @PostMapping(value = "upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file,
//                                       @RequestParam("site") String site,
//                                       Principal principal) {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "status", "error",
//                    "message", "Uploaded file is empty"
//            ));
//        }
//        return ResponseEntity.of(Map.of(
//                "status", "success",
//                "imported", importedCount,
//                "site", site
//        ));
//    }
//
//    public int importManaboxCsv(MultipartFile file, Long userId) {
//        try {
//            CsvMapper mapper = new CsvMapper();
//            CsvSchema schema = mapper
//                    .schemaFor(ManaboxCsvRow.class)
//                    .withHeader()
//                    .withColumnSeparator(',');
//            MappingIterator<ManaboxCsvRow> rows = mapper
//                    .readerFor(ManaboxCsvRow.class)
//                    .with(schema)
//                    .readValues(file.getInputStream());
//            int imported = 0;
//            while (rows.hasNext()) {
//                ManaboxCsvRow row = rows.next();
//                userCollectionService.addOrUpdateCollection(userId, row.getScryfallId(), row.getQuantity());
//                imported += collectionRepository.upsertUserCard(username, card);
//            }
//            return imported;
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to parse Manabox CSV", e);
//        }
//    }
//
//    private Card convertManaboxRow(ManaboxCsvRow row) {
//        Card card = new Card();
//
//        card.setName(row.getName());
//        card.setSet(row.getSetCode());
//        card.setCollectorNumber(normalizeCollectorNumber(row.getCollectorNumber()));
//        card.setQuantity(row.getQuantity());
//
//        card.setFoil(normalizeFoil(row.getFoil()));
//        card.setCondition(normalizeCondition(row.getCondition()));
//        card.setLanguage(normalizeLanguage(row.getLanguage()));
//
//        card.setPurchasePrice(row.getPurchasePrice());
//        card.setManaboxId(row.getManaboxId());
//        card.setScryfallId(row.getScryfallId());
//
//        return card;
//    }
//
//
//}
