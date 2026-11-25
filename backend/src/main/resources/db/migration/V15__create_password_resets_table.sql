CREATE TABLE password_resets (
    user_id           UUID         PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    code_hash         VARCHAR(100) NOT NULL,
    expires_at        TIMESTAMPTZ  NOT NULL,
    failed_attempts   INTEGER      NOT NULL,
    requested_at      TIMESTAMPTZ  NOT NULL,
    window_started_at TIMESTAMPTZ  NOT NULL,
    codes_in_window   INTEGER      NOT NULL,
    CONSTRAINT ck_password_resets_counters CHECK (failed_attempts >= 0 AND codes_in_window > 0)
);
