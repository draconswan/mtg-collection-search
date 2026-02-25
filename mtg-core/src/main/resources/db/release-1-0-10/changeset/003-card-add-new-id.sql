ALTER TABLE card
    ADD COLUMN id_new UUID;
UPDATE card
SET id_new = id::uuid;