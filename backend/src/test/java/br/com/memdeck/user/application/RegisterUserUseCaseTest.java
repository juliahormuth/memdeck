package br.com.memdeck.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.memdeck.user.domain.EmailAlreadyRegisteredException;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

class RegisterUserUseCaseTest {

	private final PasswordHasher hasher = new PasswordHasher() {
		@Override
		public String hash(String rawPassword) {
			return "hashed:" + rawPassword;
		}

		@Override
		public boolean matches(String rawPassword, String passwordHash) {
			return hash(rawPassword).equals(passwordHash);
		}
	};

	private final InMemoryUserGateway gateway = new InMemoryUserGateway();
	private final RegisterUserUseCase useCase = new RegisterUserUseCaseImpl(gateway, hasher);

	@Test
	void registersUserStoringOnlyTheHash() {
		User user = useCase.execute(new RegisterUserCommand("Julia", "Julia@Example.com", "correct-horse"));

		assertThat(user.getEmail()).isEqualTo("julia@example.com");
		assertThat(user.getPasswordHash()).isEqualTo("hashed:correct-horse");
		assertThat(gateway.findByEmail("julia@example.com")).isPresent();
	}

	@Test
	void rejectsEmailAlreadyRegisteredIgnoringCase() {
		useCase.execute(new RegisterUserCommand("Julia", "julia@example.com", "correct-horse"));

		assertThatThrownBy(() -> useCase.execute(new RegisterUserCommand("Other", "JULIA@example.com", "another-pass")))
				.isInstanceOf(EmailAlreadyRegisteredException.class);
		assertThat(gateway.size()).isEqualTo(1);
	}

	@Test
	void rejectsWeakPasswordWithoutSaving() {
		assertThatThrownBy(() -> useCase.execute(new RegisterUserCommand("Julia", "julia@example.com", "short")))
				.isInstanceOf(InvalidUserException.class);

		assertThat(gateway.size()).isZero();
	}

	@Test
	void rejectsInvalidEmailWithoutSaving() {
		assertThatThrownBy(() -> useCase.execute(new RegisterUserCommand("Julia", "not-an-email", "correct-horse")))
				.isInstanceOf(InvalidUserException.class);

		assertThat(gateway.size()).isZero();
	}

	@Test
	void commandToStringDoesNotLeakThePassword() {
		String text = new RegisterUserCommand("Julia", "julia@example.com", "correct-horse").toString();

		assertThat(text).doesNotContain("correct-horse");
	}
}
