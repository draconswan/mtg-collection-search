ALTER TABLE card ADD prices_list VARCHAR;
CREATE INDEX idx_card_prices_list ON card (prices_list);