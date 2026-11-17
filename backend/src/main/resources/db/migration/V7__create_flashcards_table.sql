CREATE TABLE flashcards (
    id         UUID          PRIMARY KEY,
    deck_id    UUID          NOT NULL,
    front      VARCHAR(500)  NOT NULL,
    back       VARCHAR(10000) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL,
    CONSTRAINT fk_flashcards_deck FOREIGN KEY (deck_id) REFERENCES decks (id) ON DELETE CASCADE
);

CREATE INDEX ix_flashcards_deck_id ON flashcards (deck_id);
