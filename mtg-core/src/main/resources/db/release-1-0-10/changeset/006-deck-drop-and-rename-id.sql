ALTER TABLE user_decks
    DROP COLUMN id;
ALTER TABLE user_decks
    RENAME COLUMN id_new TO id;
ALTER TABLE user_decks
    ADD PRIMARY KEY (id);