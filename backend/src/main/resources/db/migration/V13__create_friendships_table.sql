CREATE TABLE friendships (
    id           UUID        PRIMARY KEY,
    requester_id UUID        NOT NULL,
    addressee_id UUID        NOT NULL,
    status       VARCHAR(10) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_addressee FOREIGN KEY (addressee_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_friendships_status CHECK (status IN ('PENDING', 'ACCEPTED')),
    CONSTRAINT ck_friendships_distinct_users CHECK (requester_id <> addressee_id)
);

CREATE UNIQUE INDEX uq_friendships_pair ON friendships (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id));

CREATE INDEX ix_friendships_addressee_id ON friendships (addressee_id);

CREATE INDEX ix_friendships_requester_id ON friendships (requester_id);
