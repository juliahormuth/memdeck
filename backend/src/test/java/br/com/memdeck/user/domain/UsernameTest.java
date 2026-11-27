package br.com.memdeck.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UsernameTest {

	@ParameterizedTest
	@ValueSource(strings = { "marcia", "  Marcia  ", "@marcia", "@MARCIA" })
	void ignoresCaseBlanksAndTheLeadingAt(String typed) {
		assertThat(User.normalizeUsername(typed)).isEqualTo("marcia");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "a.b_c", "user123", "abcdefghij0123456789" })
	void acceptsLettersNumbersDotsAndUnderscores(String username) {
		assertThat(User.normalizeUsername(username)).isEqualTo(username);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "ab", "abcdefghij01234567890", "with space", "márcia", "a-b", "julia@example.com", "@@abc" })
	void rejectsEverythingElse(String username) {
		assertThatThrownBy(() -> User.normalizeUsername(username)).isInstanceOf(InvalidUserException.class);
	}
}
