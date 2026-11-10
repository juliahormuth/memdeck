package br.com.memdeck.user.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

	@Test
	void acceptsPasswordWithinLimits() {
		assertThatCode(() -> PasswordPolicy.validate("a".repeat(PasswordPolicy.MIN_LENGTH))).doesNotThrowAnyException();
		assertThatCode(() -> PasswordPolicy.validate("a".repeat(PasswordPolicy.MAX_LENGTH))).doesNotThrowAnyException();
	}

	@Test
	void rejectsShortPassword() {
		assertThatThrownBy(() -> PasswordPolicy.validate("a".repeat(PasswordPolicy.MIN_LENGTH - 1)))
				.isInstanceOf(InvalidUserException.class);
	}

	@Test
	void rejectsTooLongPassword() {
		assertThatThrownBy(() -> PasswordPolicy.validate("a".repeat(PasswordPolicy.MAX_LENGTH + 1)))
				.isInstanceOf(InvalidUserException.class);
	}

	@Test
	void rejectsNullPassword() {
		assertThatThrownBy(() -> PasswordPolicy.validate(null)).isInstanceOf(InvalidUserException.class);
	}
}
