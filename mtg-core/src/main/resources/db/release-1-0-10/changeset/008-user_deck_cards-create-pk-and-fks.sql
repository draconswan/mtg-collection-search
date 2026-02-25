ALTER TABLE user_deck_cards
    ADD PRIMARY KEY (deck_id, card_id);
ALTER TABLE user_deck_cards
    ADD FOREIGN KEY (deck_id) REFERENCES user_decks(id);
ALTER TABLE user_deck_cards
    ADD FOREIGN KEY (card_id) REFERENCES card(id);

