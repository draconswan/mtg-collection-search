CREATE TABLE user_decks (
    id BIGSERIAL PRIMARY KEY,
    deckname VARCHAR(255) NOT NULL,
    decktype VARCHAR(100) NOT NULL,
    user_id BIGSERIAL NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_deck_cards (
    deck_id BIGSERIAL NOT NULL,
    card_id varchar NOT NULL,
    quantity BIGINT NOT NULL,
    PRIMARY KEY (deck_id, card_id),
    FOREIGN KEY (deck_id) REFERENCES user_decks(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);