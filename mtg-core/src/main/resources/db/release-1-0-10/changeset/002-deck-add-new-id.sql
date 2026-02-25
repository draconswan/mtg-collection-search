ALTER TABLE user_decks
    ADD COLUMN id_new UUID DEFAULT gen_random_uuid();
UPDATE user_decks
SET id_new = gen_random_uuid()
WHERE id_new IS NULL;