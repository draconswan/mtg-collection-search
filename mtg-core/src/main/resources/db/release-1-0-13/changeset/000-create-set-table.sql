CREATE TABLE card_set
(
    code        VARCHAR(10) PRIMARY KEY,
    name        TEXT NOT NULL,
    released_at DATE,
    icon_svg    TEXT
);