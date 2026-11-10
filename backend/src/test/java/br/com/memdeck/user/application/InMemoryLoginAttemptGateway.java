package br.com.memdeck.user.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class InMemoryLoginAttemptGateway implements LoginAttemptGateway {

	private record Attempt(String email, String origin, Instant at) {
	}

	private final List<Attempt> attempts = new ArrayList<>();

	@Override
	public FailureWindow failuresSince(String email, String origin, Instant since) {
		List<Instant> matching = attempts.stream()
				.filter(a -> a.email().equals(email) && a.origin().equals(origin) && a.at().isAfter(since))
				.map(Attempt::at)
				.sorted()
				.toList();
		return new FailureWindow(matching.size(), matching.stream().findFirst());
	}

	@Override
	public void recordFailure(String email, String origin, Instant at) {
		attempts.add(new Attempt(email, origin, at));
	}

	@Override
	public void clear(String email, String origin) {
		attempts.removeIf(a -> a.email().equals(email) && a.origin().equals(origin));
	}

	int total() {
		return attempts.size();
	}
}
