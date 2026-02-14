ALTER TABLE card
    ALTER COLUMN id TYPE TEXT,
    ALTER COLUMN oracle_id TYPE TEXT,
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN scryfall_uri TYPE TEXT,
    ALTER COLUMN lang TYPE TEXT,
    ALTER COLUMN type_line TYPE TEXT,
    ALTER COLUMN color TYPE TEXT,
    ALTER COLUMN set_code TYPE TEXT,
    ALTER COLUMN set_name TYPE TEXT,
    ALTER COLUMN set_type TYPE TEXT,
    ALTER COLUMN rarity TYPE TEXT,
    ALTER COLUMN collector_number TYPE TEXT,
    ALTER COLUMN games_list TYPE TEXT,
    ALTER COLUMN flavor_name TYPE TEXT,
    ALTER COLUMN prices_list TYPE TEXT,
    ALTER COLUMN mana_cost TYPE TEXT,
    ALTER COLUMN images TYPE TEXT,
    ALTER COLUMN printed_name TYPE TEXT;

ALTER TABLE card
    ALTER COLUMN released_at TYPE DATE
        USING released_at::DATE;

ALTER TABLE users
    ALTER COLUMN email TYPE TEXT,
    ALTER COLUMN username TYPE TEXT,
    ALTER COLUMN password TYPE TEXT;

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at::timestamptz;

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE roles
    ALTER COLUMN name TYPE TEXT;

ALTER TABLE user_decks
    ALTER COLUMN deckname TYPE TEXT,
    ALTER COLUMN decktype TYPE TEXT;

ALTER TABLE user_decks
    ALTER COLUMN user_id TYPE BIGINT;

ALTER TABLE user_decks
    ALTER COLUMN created_at TYPE TIMESTAMPTZ
        USING created_at::timestamptz,
    ALTER COLUMN last_updated TYPE TIMESTAMPTZ
        USING last_updated::timestamptz;

ALTER TABLE user_decks
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN last_updated SET NOT NULL;

ALTER TABLE user_deck_cards
    ADD COLUMN location TEXT NOT NULL DEFAULT 'mainboard';

ALTER TABLE user_deck_cards
    ALTER COLUMN deck_id TYPE BIGINT;

ALTER TABLE user_deck_cards
    ALTER COLUMN card_id TYPE TEXT;