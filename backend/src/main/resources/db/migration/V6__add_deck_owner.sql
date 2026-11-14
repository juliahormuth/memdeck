-- decks created before ownership existed cannot be assigned to anyone (development data only)
DELETE FROM decks;

ALTER TABLE decks
    ADD COLUMN owner_id UUID NOT NULL,
    ADD CONSTRAINT fk_decks_owner FOREIGN KEY (owner_id) REFERENCES users (id);

CREATE INDEX ix_decks_owner_id ON decks (owner_id);
