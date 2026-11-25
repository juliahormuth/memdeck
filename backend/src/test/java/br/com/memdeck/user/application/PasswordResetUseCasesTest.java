package br.com.memdeck.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.memdeck.user.domain.InvalidPasswordResetCodeException;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.PasswordReset;
import br.com.memdeck.user.domain.User;

class PasswordResetUseCasesTest {

	private static final String EMAIL = "julia@example.com";
	private static final String NEW_PASSWORD = "brand-new-password";

	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryPasswordResetGateway resets = new InMemoryPasswordResetGateway();
	private final InMemoryLoginAttemptGateway attempts = new InMemoryLoginAttemptGateway();
	private final MutableClock clock = new MutableClock();
	private final List<String> sentCodes = new ArrayList<>();
	private final List<UUID> endedSessions = new ArrayList<>();

	private final PasswordResetCodes codes = new PasswordResetCodes() {
		private int issued;

		@Override
		public String generate() {
			return String.format("%06d", ++issued);
		}

		@Override
		public String hash(UUID userId, String code) {
			return "hashed:" + code;
		}

		@Override
		public boolean matches(UUID userId, String code, String codeHash) {
			return hash(userId, code).equals(codeHash);
		}
	};

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

	private final RequestPasswordResetUseCase request = new RequestPasswordResetUseCaseImpl(users, resets, codes,
			(email, code) -> sentCodes.add(code), clock);
	private final ConfirmPasswordResetUseCase confirm = new ConfirmPasswordResetUseCaseImpl(users, resets, codes,
			hasher, endedSessions::add, attempts, clock);

	private User user;

	@BeforeEach
	void register() {
		user = users.save(User.register("Julia", EMAIL, "hashed:old-password"));
	}

	private void confirmWith(String code) {
		confirm.execute(new ConfirmPasswordResetCommand(EMAIL, code, NEW_PASSWORD));
	}

	private String currentHash() {
		return users.findById(user.getId()).orElseThrow().getPasswordHash();
	}

	@Test
	void sendsACodeAndStoresOnlyItsHash() {
		request.execute("  Julia@Example.com ");

		assertThat(sentCodes).containsExactly("000001");
		assertThat(resets.stored.get(user.getId()).codeHash()).isEqualTo("hashed:000001");
	}

	@Test
	void staysSilentForAnUnknownEmail() {
		request.execute("nobody@example.com");

		assertThat(sentCodes).isEmpty();
		assertThat(resets.stored).isEmpty();
	}

	@Test
	void waitsBeforeSendingAnotherCode() {
		request.execute(EMAIL);
		clock.advance(PasswordReset.RESEND_COOLDOWN.minusSeconds(1));
		request.execute(EMAIL);
		assertThat(sentCodes).hasSize(1);

		clock.advance(Duration.ofSeconds(1));
		request.execute(EMAIL);
		assertThat(sentCodes).hasSize(2);
	}

	@Test
	void limitsTheNumberOfCodesPerDay() {
		for (int i = 0; i < PasswordReset.MAX_CODES_PER_WINDOW + 3; i++) {
			request.execute(EMAIL);
			clock.advance(PasswordReset.RESEND_COOLDOWN);
		}
		assertThat(sentCodes).hasSize(PasswordReset.MAX_CODES_PER_WINDOW);

		clock.advance(PasswordReset.CODE_WINDOW);
		request.execute(EMAIL);
		assertThat(sentCodes).hasSize(PasswordReset.MAX_CODES_PER_WINDOW + 1);
	}

	@Test
	void changesThePasswordEndsSessionsAndUnblocksLogin() {
		attempts.recordFailure(EMAIL, "127.0.0.1", clock.instant());
		request.execute(EMAIL);

		confirmWith(sentCodes.getLast());

		assertThat(currentHash()).isEqualTo("hashed:" + NEW_PASSWORD);
		assertThat(endedSessions).containsExactly(user.getId());
		assertThat(attempts.failuresSince(EMAIL, "127.0.0.1", Instant.EPOCH).count()).isZero();
	}

	@Test
	void acceptsACodeOnlyOnce() {
		request.execute(EMAIL);
		confirmWith(sentCodes.getLast());

		assertThatThrownBy(() -> confirmWith(sentCodes.getLast())).isInstanceOf(InvalidPasswordResetCodeException.class);
	}

	@Test
	void rejectsAnExpiredCode() {
		request.execute(EMAIL);
		clock.advance(PasswordReset.CODE_LIFETIME);

		assertThatThrownBy(() -> confirmWith(sentCodes.getLast())).isInstanceOf(InvalidPasswordResetCodeException.class);
		assertThat(currentHash()).isEqualTo("hashed:old-password");
	}

	@Test
	void burnsTheCodeAfterTooManyWrongGuesses() {
		request.execute(EMAIL);
		for (int i = 0; i < PasswordReset.MAX_FAILED_ATTEMPTS; i++) {
			assertThatThrownBy(() -> confirmWith("999999")).isInstanceOf(InvalidPasswordResetCodeException.class);
		}

		assertThatThrownBy(() -> confirmWith(sentCodes.getLast())).isInstanceOf(InvalidPasswordResetCodeException.class);
		assertThat(currentHash()).isEqualTo("hashed:old-password");
	}

	@Test
	void invalidatesThePreviousCodeWhenANewOneIsSent() {
		request.execute(EMAIL);
		clock.advance(PasswordReset.RESEND_COOLDOWN);
		request.execute(EMAIL);

		assertThatThrownBy(() -> confirmWith(sentCodes.getFirst())).isInstanceOf(InvalidPasswordResetCodeException.class);
		confirmWith(sentCodes.getLast());
	}

	@Test
	void answersAnUnknownEmailLikeAWrongCode() {
		assertThatThrownBy(() -> confirm.execute(
				new ConfirmPasswordResetCommand("nobody@example.com", "000001", NEW_PASSWORD)))
				.isInstanceOf(InvalidPasswordResetCodeException.class);
	}

	@Test
	void refusesAWeakPasswordWithoutSpendingTheCode() {
		request.execute(EMAIL);

		assertThatThrownBy(() -> confirm.execute(new ConfirmPasswordResetCommand(EMAIL, sentCodes.getLast(), "short")))
				.isInstanceOf(InvalidUserException.class);
		confirmWith(sentCodes.getLast());
	}
}
