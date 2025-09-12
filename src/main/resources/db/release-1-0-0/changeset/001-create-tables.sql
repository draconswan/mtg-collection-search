CREATE TABLE IF NOT EXISTS "data_version"
(
    id varchar,
    last_refresh timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "card"
(
    id varchar,
    oracle_id varchar,
    name varchar,
    scryfall_uri varchar,
    lang varchar,
    released_at varchar,
    set_code varchar,
    set_name varchar,
    set_type varchar,
    rarity varchar,
    collector_number varchar,
    games_list varchar,
    PRIMARY KEY ("id")
);

CREATE INDEX idx_set_code ON "card" ("set_code");
CREATE INDEX idx_card_name ON "card" ("name");
CREATE INDEX idx_set_name ON "card" ("set_name");
CREATE INDEX idx_card_lang ON "card" ("lang");