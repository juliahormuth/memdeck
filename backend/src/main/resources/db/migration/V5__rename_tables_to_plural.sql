ALTER TABLE deck RENAME TO decks;
ALTER INDEX deck_pkey RENAME TO decks_pkey;
ALTER TABLE decks RENAME CONSTRAINT ck_deck_visibility TO ck_decks_visibility;

ALTER TABLE login_attempt RENAME TO login_attempts;
ALTER INDEX login_attempt_pkey RENAME TO login_attempts_pkey;
ALTER INDEX ix_login_attempt_key RENAME TO ix_login_attempts_key;
ALTER INDEX ix_login_attempt_attempted_at RENAME TO ix_login_attempts_attempted_at;
