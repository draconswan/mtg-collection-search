ALTER TABLE card ADD mana_cost VARCHAR;
CREATE INDEX idx_card_mana_cost ON card (mana_cost);