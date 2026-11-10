package br.com.memdeck.user.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.domain.InvalidCredentialsException;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.PasswordPolicy;
import br.com.memdeck.user.domain.TooManyLoginAttemptsException;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(noRollbackFor = InvalidCredentialsException.class)
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

	private static final int KEY_MAX_LENGTH = 254;
	private static final int ORIGIN_MAX_LENGTH = 64;

	private final UserGateway gateway;
	private final PasswordHasher passwordHasher;
	private final LoginAttemptGateway attempts;
	private final LoginAttemptPolicy policy;
	private final Clock clock;
	// hashed once so an unknown email costs the same as a wrong password (no user enumeration by timing)
	private final String dummyHash;

	public AuthenticateUserUseCaseImpl(UserGateway gateway, PasswordHasher passwordHasher,
			LoginAttemptGateway attempts, LoginAttemptPolicy policy, Clock clock) {
		this.gateway = gateway;
		this.passwordHasher = passwordHasher;
		this.attempts = attempts;
		this.policy = policy;
		this.clock = clock;
		this.dummyHash = passwordHasher.hash("dummy-password-for-timing");
	}

	@Override
	public User execute(AuthenticateUserCommand command) {
		Instant now = clock.instant();
		String key = attemptKey(command.email());
		String origin = truncate(command.origin(), ORIGIN_MAX_LENGTH);

		FailureWindow failures = attempts.failuresSince(key, origin, now.minus(policy.window()));
		if (failures.count() >= policy.maxFailures()) {
			throw new TooManyLoginAttemptsException(retryAfter(failures, now));
		}

		try {
			User user = verify(command);
			attempts.clear(key, origin);
			return user;
		}
		catch (InvalidCredentialsException exception) {
			attempts.recordFailure(key, origin, now);
			throw exception;
		}
	}

	private User verify(AuthenticateUserCommand command) {
		String password = command.password();
		if (password == null || password.length() > PasswordPolicy.MAX_LENGTH) {
			throw new InvalidCredentialsException();
		}
		Optional<User> user = findByEmail(command.email());
		String hash = user.map(User::getPasswordHash).orElse(dummyHash);
		boolean matches = passwordHasher.matches(password, hash);
		if (user.isEmpty() || !matches) {
			throw new InvalidCredentialsException();
		}
		return user.get();
	}

	private Optional<User> findByEmail(String email) {
		try {
			return gateway.findByEmail(User.normalizeEmail(email));
		}
		catch (InvalidUserException exception) {
			return Optional.empty();
		}
	}

	// unknown and malformed emails are counted too, so blocking never reveals which accounts exist
	private String attemptKey(String email) {
		try {
			return User.normalizeEmail(email);
		}
		catch (InvalidUserException exception) {
			return truncate(email == null ? "" : email.strip().toLowerCase(Locale.ROOT), KEY_MAX_LENGTH);
		}
	}

	private Duration retryAfter(FailureWindow failures, Instant now) {
		Instant release = failures.oldest().orElse(now).plus(policy.window());
		Duration wait = Duration.between(now, release);
		return wait.isNegative() || wait.isZero() ? Duration.ofSeconds(1) : wait;
	}

	private static String truncate(String value, int max) {
		if (value == null) {
			return "";
		}
		return value.length() > max ? value.substring(0, max) : value;
	}
}
