package br.com.memdeck.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import br.com.memdeck.user.domain.InvalidCredentialsException;
import br.com.memdeck.user.domain.TooManyLoginAttemptsException;
import br.com.memdeck.user.domain.User;

class LoginThrottlingTest {

	private static final String ORIGIN = "10.0.0.1";

	private static class CountingHasher implements PasswordHasher {

		int matchCalls;

		@Override
		public String hash(String rawPassword) {
			return "hashed:" + rawPassword;
		}

		@Override
		public boolean matches(String rawPassword, String passwordHash) {
			matchCalls++;
			return hash(rawPassword).equals(passwordHash);
		}
	}

	private final CountingHasher hasher = new CountingHasher();
	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryLoginAttemptGateway attempts = new InMemoryLoginAttemptGateway();
	private final MutableClock clock = new MutableClock();
	private final AuthenticateUserUseCase useCase = new AuthenticateUserUseCaseImpl(users, hasher, attempts,
			new LoginAttemptPolicy(5, Duration.ofMinutes(15)), clock);

	private AuthenticateUserCommand attempt(String email, String password, String origin) {
		return new AuthenticateUserCommand(email, password, origin);
	}

	private void fail(String email, String origin, int times) {
		for (int i = 0; i < times; i++) {
			assertThatThrownBy(() -> useCase.execute(attempt(email, "wrong-password", origin)))
					.isInstanceOf(InvalidCredentialsException.class);
		}
	}

	private void registerJulia() {
		users.save(User.register("Julia", "julia@example.com", hasher.hash("correct-horse")));
	}

	@Test
	void blocksTheSixthAttemptEvenWithTheRightPassword() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 5);

		assertThatThrownBy(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.isInstanceOf(TooManyLoginAttemptsException.class);
	}

	@Test
	void allowsFourFailuresAndThenARightPassword() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 4);

		assertThatCode(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.doesNotThrowAnyException();
	}

	@Test
	void aSuccessfulLoginResetsTheCounter() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 4);
		useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN));

		fail("julia@example.com", ORIGIN, 4);

		assertThatCode(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.doesNotThrowAnyException();
	}

	@Test
	void theBlockEndsWhenTheOldestFailureLeavesTheWindow() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 5);

		clock.advance(Duration.ofMinutes(15).plusSeconds(1));

		assertThatCode(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.doesNotThrowAnyException();
	}

	@Test
	void tellsHowLongToWait() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 5);
		clock.advance(Duration.ofMinutes(5));

		assertThatThrownBy(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.isInstanceOfSatisfying(TooManyLoginAttemptsException.class,
						exception -> assertThat(exception.getRetryAfter()).isEqualTo(Duration.ofMinutes(10)));
	}

	@Test
	void blockedAttemptsAreNotHashedNorRecorded() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 5);
		int matchCallsBefore = hasher.matchCalls;
		int recordedBefore = attempts.total();

		assertThatThrownBy(() -> useCase.execute(attempt("julia@example.com", "correct-horse", ORIGIN)))
				.isInstanceOf(TooManyLoginAttemptsException.class);

		assertThat(hasher.matchCalls).isEqualTo(matchCallsBefore);
		assertThat(attempts.total()).isEqualTo(recordedBefore);
	}

	@Test
	void anotherOriginIsNotAffected() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 5);

		assertThatCode(() -> useCase.execute(attempt("julia@example.com", "correct-horse", "10.0.0.2")))
				.doesNotThrowAnyException();
	}

	@Test
	void anotherAccountOnTheSameOriginIsNotAffected() {
		registerJulia();
		users.save(User.register("Other", "other@example.com", hasher.hash("other-password")));
		fail("julia@example.com", ORIGIN, 5);

		assertThatCode(() -> useCase.execute(attempt("other@example.com", "other-password", ORIGIN)))
				.doesNotThrowAnyException();
	}

	@Test
	void unknownEmailsAreBlockedTooSoBlockingNeverRevealsWhichAccountsExist() {
		fail("ghost@example.com", ORIGIN, 5);

		assertThatThrownBy(() -> useCase.execute(attempt("ghost@example.com", "whatever-password", ORIGIN)))
				.isInstanceOf(TooManyLoginAttemptsException.class);
	}

	@Test
	void emailCaseDoesNotEscapeTheLimit() {
		registerJulia();
		fail("julia@example.com", ORIGIN, 3);
		fail("JULIA@Example.com", ORIGIN, 2);

		assertThatThrownBy(() -> useCase.execute(attempt(" Julia@EXAMPLE.com ", "correct-horse", ORIGIN)))
				.isInstanceOf(TooManyLoginAttemptsException.class);
	}
}
