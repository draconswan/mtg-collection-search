package com.dswan.mtg.model;

import com.dswan.mtg.domain.cards.*;
import com.dswan.mtg.domain.model.CardStateForm;
import com.dswan.mtg.domain.model.DeckStateForm;
import com.dswan.mtg.util.DeckProcessingUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class DeckViewModel {
    private String deckId;
    private String deckName;
    private DeckFormat deckFormat;
    private List<DeckZoneGroup> zones;

    private int totalQuantity;
    private int totalProxies;

    private DeckStateForm deckStateForm;

    private Map<String, List<String>> deckFormats;
    private List<String> cardsNotFound;

    public static DeckViewModel buildDeckViewModel(Deck deck) {
        DeckFormat format = DeckFormat.fromString(deck.getDeckType());

        AtomicInteger idx = new AtomicInteger();
        List<CardEntry> cardEntries = deck.getCards().stream()
                .map(card -> new CardEntry(idx.getAndIncrement(), card.getQuantity(), card))
                .toList();

        List<DeckZoneGroup> zones = new ArrayList<>();
        for (DeckZone zone : format.zones) {
            List<CardEntry> zoneEntries = cardEntries.stream()
                    .filter(e -> DeckZone.fromString(e.getCard().getLocation()) == zone)
                    .toList();

            Map<String, Integer> typeQuantities = new LinkedHashMap<>();
            Map<String, List<CardEntry>> orderedCardGroups =
                    DeckProcessingUtil.getOrderedCardGroups(zoneEntries, typeQuantities);

            DeckZoneGroup zoneGroup = new DeckZoneGroup();
            zoneGroup.setZone(zone);

            List<CardGroup> typeGroups = orderedCardGroups.entrySet().stream()
                    .map(e -> new CardGroup(e.getKey(), e.getValue()))
                    .toList();

            zoneGroup.setGroups(typeGroups);
            zones.add(zoneGroup);
        }

        DeckStateForm form = new DeckStateForm();
        form.setDeckId(deck.getId());
        form.setDeckName(deck.getName());
        form.setDeckFormat(deck.getType());
        form.setCards(deck.getCards().stream()
                .map(card -> {
                    CardStateForm cardStateForm = new CardStateForm();
                    cardStateForm.setCardId(card.getId());
                    cardStateForm.setQuantity(card.getQuantity());
                    cardStateForm.setChecked(card.isChecked());
                    cardStateForm.setZone(card.getLocation());
                    return cardStateForm;
                })
                .toList());
        DeckViewModel vm = new DeckViewModel();
        vm.setDeckId(deck.getId());
        vm.setDeckName(deck.getName());
        vm.setDeckFormat(format);
        vm.setZones(zones);
        vm.setTotalQuantity(zones.stream().mapToInt(DeckZoneGroup::getTotalQuantity).sum());
        vm.setTotalProxies(zones.stream().mapToInt(DeckZoneGroup::getTotalProxies).sum());
        vm.setDeckStateForm(form);
        vm.setDeckFormats(DeckFormats.FORMATS);
        vm.setCardsNotFound(List.of());
        return vm;
    }

    public static DeckViewModel fromRawDecklist(List<CardEntry> cardEntries,
                                                List<String> cardsNotFound) {
        // Everything goes in MAINBOARD
        DeckZone zone = DeckZone.MAINBOARD;
        // Group by type
        Map<String, Integer> typeQuantities = new LinkedHashMap<>();
        Map<String, List<CardEntry>> orderedCardGroups = DeckProcessingUtil.getOrderedCardGroups(cardEntries, typeQuantities);
        List<CardGroup> typeGroups = orderedCardGroups.entrySet().stream()
                .map(e -> new CardGroup(e.getKey(), e.getValue()))
                .toList();
        DeckZoneGroup zoneGroup = new DeckZoneGroup(zone, typeGroups);
        // Build form
        DeckStateForm form = new DeckStateForm();
        form.setCards(cardEntries.stream()
                .map(entry -> {
                    CardStateForm cardStateForm = new CardStateForm();
                    cardStateForm.setCardId(entry.getCard().getId());
                    cardStateForm.setQuantity(entry.getQuantity());
                    cardStateForm.setChecked(false);
                    cardStateForm.setProxy(false);
                    cardStateForm.setZone(DeckZone.MAINBOARD.name().toLowerCase());
                    return cardStateForm;
                })
                .toList());
        DeckViewModel vm = new DeckViewModel();
        vm.setDeckId(null); // no deck yet
        vm.setDeckName("New Decklist");
        vm.setDeckFormat(DeckFormat.DEFAULT);
        vm.setZones(List.of(zoneGroup));
        vm.setTotalQuantity(zoneGroup.getTotalQuantity());
        vm.setTotalProxies(zoneGroup.getTotalProxies());
        vm.setDeckStateForm(form);
        vm.setCardsNotFound(cardsNotFound);
        vm.setDeckFormats(DeckFormats.FORMATS);
        return vm;
    }
}