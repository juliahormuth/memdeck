package br.com.memdeck.user.application;

import java.time.Instant;

public interface LoginAttemptGateway {

	FailureWindow failuresSince(String email, String origin, Instant since);

	void recordFailure(String email, String origin, Instant at);

	void clear(String email, String origin);
}
