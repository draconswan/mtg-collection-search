ALTER TABLE card ADD printed_name VARCHAR;
CREATE INDEX idx_printed_name_uris ON card (printed_name);