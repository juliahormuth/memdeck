package br.com.memdeck.user.infrastructure;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.application.LoginAttemptPolicy;

@Component
class LoginAttemptCleanup {

	private final LoginAttemptJpaRepository repository;
	private final LoginAttemptPolicy policy;
	private final Clock clock;

	LoginAttemptCleanup(LoginAttemptJpaRepository repository, LoginAttemptPolicy policy, Clock clock) {
		this.repository = repository;
		this.policy = policy;
		this.clock = clock;
	}

	@Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1M")
	@Transactional
	void deleteExpiredAttempts() {
		repository.deleteOlderThan(clock.instant().minus(policy.window()));
	}
}
