CREATE INDEX idx_user_decks_user_id ON user_decks (user_id);
CREATE INDEX idx_card_set_code ON card (set_code);
CREATE INDEX idx_card_names ON card (name, printed_name, flavor_name);
