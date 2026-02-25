ALTER TABLE user_deck_cards
    ADD COLUMN deck_id_new UUID;

UPDATE user_deck_cards dc
SET deck_id_new = d.id_new
FROM user_decks d
WHERE dc.deck_id = d.id;

ALTER TABLE user_deck_cards
    ADD COLUMN card_id_new UUID;

UPDATE user_deck_cards dc
SET card_id_new = c.id_new
FROM card c
WHERE dc.card_id = c.id;