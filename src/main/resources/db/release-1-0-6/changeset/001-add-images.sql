ALTER TABLE card ADD images VARCHAR;
CREATE INDEX idx_card_image_uris ON card (images);