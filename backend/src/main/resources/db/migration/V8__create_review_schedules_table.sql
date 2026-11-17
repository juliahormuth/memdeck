CREATE TABLE review_schedules (
    id                UUID         PRIMARY KEY,
    user_id           UUID         NOT NULL,
    flashcard_id      UUID         NOT NULL,
    repetitions       INTEGER      NOT NULL,
    interval_days     INTEGER      NOT NULL,
    ease_factor       NUMERIC(4,2) NOT NULL,
    due_date          DATE         NOT NULL,
    first_reviewed_on DATE         NOT NULL,
    last_reviewed_on  DATE         NOT NULL,
    CONSTRAINT fk_review_schedules_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_schedules_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards (id) ON DELETE CASCADE,
    CONSTRAINT uq_review_schedules_user_flashcard UNIQUE (user_id, flashcard_id),
    CONSTRAINT ck_review_schedules_ease_factor CHECK (ease_factor >= 1.30)
);

CREATE INDEX ix_review_schedules_flashcard_id ON review_schedules (flashcard_id);
