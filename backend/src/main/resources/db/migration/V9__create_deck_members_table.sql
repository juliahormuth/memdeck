CREATE TABLE deck_members (
    id        UUID        PRIMARY KEY,
    deck_id   UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    role      VARCHAR(10) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_deck_members_deck FOREIGN KEY (deck_id) REFERENCES decks (id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_deck_members_deck_user UNIQUE (deck_id, user_id),
    CONSTRAINT ck_deck_members_role CHECK (role IN ('EDITOR', 'READER'))
);

CREATE INDEX ix_deck_members_user_id ON deck_members (user_id);
