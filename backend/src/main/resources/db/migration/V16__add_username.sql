ALTER TABLE users ADD COLUMN username VARCHAR(20);

-- accounts created before usernames existed get a generated one
UPDATE users SET username = 'user_' || substr(md5(id::text), 1, 10);

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL,
    ADD CONSTRAINT uq_users_username UNIQUE (username),
    ADD CONSTRAINT ck_users_username CHECK (username ~ '^[a-z0-9._]{3,20}$');
