package br.com.memdeck.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import br.com.memdeck.user.domain.InvalidCredentialsException;
import br.com.memdeck.user.domain.PasswordPolicy;
import br.com.memdeck.user.domain.User;

class AuthenticateUserUseCaseTest {

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
	private final InMemoryUserGateway gateway = new InMemoryUserGateway();
	private final AuthenticateUserUseCase useCase = new AuthenticateUserUseCaseImpl(gateway, hasher,
			new InMemoryLoginAttemptGateway(), new LoginAttemptPolicy(5, Duration.ofMinutes(15)), new MutableClock());

	private User registered() {
		return gateway.save(User.register("Julia", "julia@example.com", hasher.hash("correct-horse")));
	}

	@Test
	void authenticatesWithTheRightPasswordIgnoringEmailCase() {
		User user = registered();

		User result = useCase.execute(new AuthenticateUserCommand(" JULIA@Example.com ", "correct-horse", "10.0.0.1"));

		assertThat(result.getId()).isEqualTo(user.getId());
	}

	@Test
	void rejectsAWrongPassword() {
		registered();

		assertThatThrownBy(() -> useCase.execute(new AuthenticateUserCommand("julia@example.com", "wrong-horse", "10.0.0.1")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void rejectsAnUnknownEmailWithTheSameErrorAndStillSpendsAHashComparison() {
		hasher.matchCalls = 0;

		assertThatThrownBy(() -> useCase.execute(new AuthenticateUserCommand("nobody@example.com", "correct-horse", "10.0.0.1")))
				.isInstanceOf(InvalidCredentialsException.class);

		assertThat(hasher.matchCalls).isEqualTo(1);
	}

	@Test
	void rejectsAMalformedEmailAsInvalidCredentials() {
		assertThatThrownBy(() -> useCase.execute(new AuthenticateUserCommand("not-an-email", "correct-horse", "10.0.0.1")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void rejectsAnOversizedPasswordWithoutHashingIt() {
		registered();
		hasher.matchCalls = 0;
		String huge = "a".repeat(PasswordPolicy.MAX_LENGTH + 1);

		assertThatThrownBy(() -> useCase.execute(new AuthenticateUserCommand("julia@example.com", huge, "10.0.0.1")))
				.isInstanceOf(InvalidCredentialsException.class);

		assertThat(hasher.matchCalls).isZero();
	}

	@Test
	void commandToStringDoesNotLeakThePassword() {
		assertThat(new AuthenticateUserCommand("julia@example.com", "correct-horse", "10.0.0.1").toString())
				.doesNotContain("correct-horse");
	}
}
