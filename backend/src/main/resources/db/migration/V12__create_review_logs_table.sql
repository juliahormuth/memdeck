CREATE TABLE review_logs (
    id           UUID        PRIMARY KEY,
    user_id      UUID        NOT NULL,
    deck_id      UUID        NOT NULL,
    flashcard_id UUID        NOT NULL,
    reviewed_on  DATE        NOT NULL,
    CONSTRAINT fk_review_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_logs_deck FOREIGN KEY (deck_id) REFERENCES decks (id) ON DELETE CASCADE,
    CONSTRAINT uq_review_logs_user_flashcard_day UNIQUE (user_id, flashcard_id, reviewed_on)
);

CREATE INDEX ix_review_logs_user_day ON review_logs (user_id, reviewed_on);

CREATE INDEX ix_review_logs_deck_user ON review_logs (deck_id, user_id);
