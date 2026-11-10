package br.com.memdeck.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class Argon2PepperPasswordHasherTest {

	private static String pepper(String seed) {
		return Base64.getEncoder().encodeToString((seed + "-padding-to-reach-32-bytes-0123456789").getBytes(StandardCharsets.UTF_8));
	}

	private final Argon2PepperPasswordHasher hasher = new Argon2PepperPasswordHasher(pepper("pepper-a"));

	@Test
	void producesAnArgon2idHashThatDoesNotContainThePassword() {
		String hash = hasher.hash("correct-horse");

		assertThat(hash).startsWith("$argon2id$");
		assertThat(hash).doesNotContain("correct-horse");
	}

	@Test
	void matchesTheRightPasswordAndRejectsTheWrongOne() {
		String hash = hasher.hash("correct-horse");

		assertThat(hasher.matches("correct-horse", hash)).isTrue();
		assertThat(hasher.matches("wrong-horse", hash)).isFalse();
	}

	@Test
	void usesADifferentSaltEachTime() {
		assertThat(hasher.hash("correct-horse")).isNotEqualTo(hasher.hash("correct-horse"));
	}

	@Test
	void aDifferentPepperCannotVerifyTheHash() {
		String hash = hasher.hash("correct-horse");
		Argon2PepperPasswordHasher otherPepper = new Argon2PepperPasswordHasher(pepper("pepper-b"));

		assertThat(otherPepper.matches("correct-horse", hash)).isFalse();
	}

	@Test
	void rejectsAPepperThatIsTooShort() {
		String shortPepper = Base64.getEncoder().encodeToString("too-short".getBytes(StandardCharsets.UTF_8));

		assertThatThrownBy(() -> new Argon2PepperPasswordHasher(shortPepper)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void rejectsAPepperThatIsNotBase64() {
		assertThatThrownBy(() -> new Argon2PepperPasswordHasher("***not base64***")).isInstanceOf(IllegalStateException.class);
	}
}
