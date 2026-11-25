package br.com.memdeck.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class HmacPasswordResetCodesTest {

	private final HmacPasswordResetCodes codes = new HmacPasswordResetCodes(
			Base64.getEncoder().encodeToString(new byte[Pepper.MIN_BYTES]));

	@Test
	void generatesSixDigitCodes() {
		assertThat(Stream.generate(codes::generate).limit(200)).allMatch(code -> code.matches("\\d{6}"));
	}

	@Test
	void bindsTheHashToTheUserAndNeverStoresTheCode() {
		UUID owner = UUID.randomUUID();
		String hash = codes.hash(owner, "123456");

		assertThat(hash).doesNotContain("123456");
		assertThat(codes.matches(owner, "123456", hash)).isTrue();
		assertThat(codes.matches(owner, "123457", hash)).isFalse();
		assertThat(codes.matches(UUID.randomUUID(), "123456", hash)).isFalse();
	}
}
