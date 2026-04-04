ALTER TABLE user_deck_cards
    ADD COLUMN proxy BOOLEAN DEFAULT FALSE;

ALTER TABLE user_deck_cards
    ADD CONSTRAINT chk_proxy_requires_checked
        CHECK (proxy = FALSE OR checked = TRUE);
