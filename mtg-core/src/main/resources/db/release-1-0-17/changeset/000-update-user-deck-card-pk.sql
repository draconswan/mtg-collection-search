ALTER TABLE user_deck_cards
    DROP CONSTRAINT user_deck_cards_pkey;

ALTER TABLE user_deck_cards
    ADD CONSTRAINT user_deck_cards_pkey
        PRIMARY KEY (deck_id, card_id, location);
