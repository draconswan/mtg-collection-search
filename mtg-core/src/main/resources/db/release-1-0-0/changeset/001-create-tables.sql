CREATE TABLE IF NOT EXISTS data_version (
    id VARCHAR,
    last_refresh TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS card (
    id VARCHAR,
    oracle_id VARCHAR,
    name VARCHAR,
    scryfall_uri VARCHAR,
    lang VARCHAR,
    released_at VARCHAR,
    type_line VARCHAR,
    color VARCHAR,
    set_code VARCHAR,
    set_name VARCHAR,
    set_type VARCHAR,
    rarity VARCHAR,
    collector_number VARCHAR,
    games_list VARCHAR,
    PRIMARY KEY (id)
);

CREATE INDEX idx_set_code ON card (set_code);
CREATE INDEX idx_card_name ON card (name);
CREATE INDEX idx_set_name ON card (set_name);
CREATE INDEX idx_card_lang ON card (lang);
CREATE INDEX idx_rarity ON card (rarity);
CREATE INDEX idx_color ON card (color);