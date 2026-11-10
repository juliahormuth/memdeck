CREATE TABLE login_attempt (
    id           UUID         PRIMARY KEY,
    email        VARCHAR(254) NOT NULL,
    origin       VARCHAR(64)  NOT NULL,
    attempted_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_login_attempt_key ON login_attempt (email, origin, attempted_at);
CREATE INDEX ix_login_attempt_attempted_at ON login_attempt (attempted_at);
