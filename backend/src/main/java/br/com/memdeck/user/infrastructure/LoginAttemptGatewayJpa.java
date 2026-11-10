package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.FailureWindow;
import br.com.memdeck.user.application.LoginAttemptGateway;

@Component
class LoginAttemptGatewayJpa implements LoginAttemptGateway {

	private final LoginAttemptJpaRepository repository;

	LoginAttemptGatewayJpa(LoginAttemptJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public FailureWindow failuresSince(String email, String origin, Instant since) {
		int count = repository.countByEmailAndOriginAndAttemptedAtAfter(email, origin, since);
		var oldest = repository.findFirstByEmailAndOriginAndAttemptedAtAfterOrderByAttemptedAtAsc(email, origin, since)
				.map(LoginAttemptJpaModel::getAttemptedAt);
		return new FailureWindow(count, oldest);
	}

	@Override
	public void recordFailure(String email, String origin, Instant at) {
		repository.save(new LoginAttemptJpaModel(UUID.randomUUID(), email, origin, at));
	}

	@Override
	public void clear(String email, String origin) {
		repository.deleteByKey(email, origin);
	}
}
