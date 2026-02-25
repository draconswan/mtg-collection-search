ALTER TABLE user_deck_cards
    DROP COLUMN card_id;

ALTER TABLE user_deck_cards
    DROP COLUMN deck_id;

ALTER TABLE user_deck_cards
    RENAME COLUMN card_id_new TO card_id;

ALTER TABLE user_deck_cards
    RENAME COLUMN deck_id_new TO deck_id;