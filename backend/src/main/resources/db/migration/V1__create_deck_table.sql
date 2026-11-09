CREATE TABLE deck (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    visibility  VARCHAR(10)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_deck_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);
