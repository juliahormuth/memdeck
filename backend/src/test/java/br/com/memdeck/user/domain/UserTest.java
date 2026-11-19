package br.com.memdeck.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void registersUserWithNormalizedEmailAndTrimmedName() {
		User user = User.register("  Julia  ", "  Julia@Example.COM ", "hash");

		assertThat(user.getId()).isNotNull();
		assertThat(user.getName()).isEqualTo("Julia");
		assertThat(user.getEmail()).isEqualTo("julia@example.com");
		assertThat(user.getPasswordHash()).isEqualTo("hash");
		assertThat(user.getCreatedAt()).isNotNull();
	}

	@Test
	void rejectsBlankName() {
		assertThatThrownBy(() -> User.register("  ", "a@b.com", "hash")).isInstanceOf(InvalidUserException.class);
	}

	@Test
	void rejectsTooLongName() {
		String name = "a".repeat(User.NAME_MAX_LENGTH + 1);

		assertThatThrownBy(() -> User.register(name, "a@b.com", "hash")).isInstanceOf(InvalidUserException.class);
	}

	@Test
	void rejectsInvalidEmails() {
		for (String email : new String[] { null, "", "   ", "no-at-sign", "a@b", "a b@c.com", "a@@b.com" }) {
			assertThatThrownBy(() -> User.register("Julia", email, "hash"))
					.as("email: %s", email)
					.isInstanceOf(InvalidUserException.class);
		}
	}

	@Test
	void rejectsMissingPasswordHash() {
		assertThatThrownBy(() -> User.register("Julia", "a@b.com", " ")).isInstanceOf(InvalidUserException.class);
	}

	@Test
	void aNewUserGetsTheDefaultAvatar() {
		assertThat(User.register("Julia", "julia@example.com", "hash").getAvatar()).isEqualTo(Avatar.BOOK);
	}

	@Test
	void changesTheNameAndAvatarKeepingIdentityEmailAndPassword() {
		User user = User.register("Julia", "julia@example.com", "hash");

		User changed = user.withProfile("  Julia Hormuth ", Avatar.GLOBE);

		assertThat(changed.getId()).isEqualTo(user.getId());
		assertThat(changed.getEmail()).isEqualTo("julia@example.com");
		assertThat(changed.getPasswordHash()).isEqualTo("hash");
		assertThat(changed.getName()).isEqualTo("Julia Hormuth");
		assertThat(changed.getAvatar()).isEqualTo(Avatar.GLOBE);
	}

	@Test
	void rejectsAnInvalidProfileChange() {
		User user = User.register("Julia", "julia@example.com", "hash");

		assertThatThrownBy(() -> user.withProfile(" ", Avatar.BOOK)).isInstanceOf(InvalidUserException.class);
		assertThatThrownBy(() -> user.withProfile("Julia", null)).isInstanceOf(InvalidUserException.class);
	}
}
