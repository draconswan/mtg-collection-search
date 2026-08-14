CREATE TABLE IF NOT EXISTS user_card_collection
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     BIGINT NOT NULL,
    card_id     UUID   NOT NULL,
    quantity    INT    NOT NULL  DEFAULT 0,
    condition   TEXT,
    is_foil     BOOLEAN,
    acquired_at TIMESTAMPTZ,
    notes       TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES card (id) ON DELETE CASCADE,
    UNIQUE (user_id, card_id)
);

CREATE INDEX idx_ucc_user ON user_card_collection (user_id);
CREATE INDEX idx_ucc_card ON user_card_collection (card_id);
CREATE INDEX idx_ucc_user_card ON user_card_collection (user_id, card_id);

CREATE TABLE IF NOT EXISTS user_card_deck_assignments
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_collection_id UUID NOT NULL,
    deck_id            UUID NOT NULL,
    assigned_quantity  INT  NOT NULL    DEFAULT 1,
    FOREIGN KEY (user_collection_id) REFERENCES user_card_collection (id) ON DELETE CASCADE,
    FOREIGN KEY (deck_id) REFERENCES user_decks (id) ON DELETE CASCADE
);

CREATE INDEX idx_ucda_deck ON user_card_deck_assignments (deck_id);
CREATE INDEX idx_ucda_collection ON user_card_deck_assignments (user_collection_id);
