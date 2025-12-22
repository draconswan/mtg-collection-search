ALTER TABLE card ADD flavor_name VARCHAR;
CREATE INDEX idx_card_flavor_name ON card (flavor_name);