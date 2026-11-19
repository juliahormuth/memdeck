CREATE TABLE deck_invitations (
    id           UUID        PRIMARY KEY,
    deck_id      UUID        NOT NULL,
    invitee_id   UUID        NOT NULL,
    inviter_id   UUID        NOT NULL,
    role         VARCHAR(10) NOT NULL,
    status       VARCHAR(10) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    CONSTRAINT fk_deck_invitations_deck FOREIGN KEY (deck_id) REFERENCES decks (id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_invitations_invitee FOREIGN KEY (invitee_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_invitations_inviter FOREIGN KEY (inviter_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_deck_invitations_role CHECK (role IN ('EDITOR', 'READER')),
    CONSTRAINT ck_deck_invitations_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED'))
);

CREATE UNIQUE INDEX uq_deck_invitations_pending ON deck_invitations (deck_id, invitee_id) WHERE status = 'PENDING';

CREATE INDEX ix_deck_invitations_invitee_id ON deck_invitations (invitee_id);
