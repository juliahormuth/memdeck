package br.com.memdeck.user.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.PasswordResetCodes;

// a keyed hash is enough here: the pepper is what stops a leaked table from being brute-forced
@Component
class HmacPasswordResetCodes implements PasswordResetCodes {

	private final SecureRandom random = new SecureRandom();
	private final Pepper pepper;

	HmacPasswordResetCodes(@Value("${memdeck.security.password-pepper}") String pepperBase64) {
		this.pepper = new Pepper(pepperBase64);
	}

	@Override
	public String generate() {
		return String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
	}

	@Override
	public String hash(UUID userId, String code) {
		return pepper.sign("password-reset:" + userId + ":" + code);
	}

	@Override
	public boolean matches(UUID userId, String code, String codeHash) {
		return MessageDigest.isEqual(hash(userId, code).getBytes(StandardCharsets.UTF_8),
				codeHash.getBytes(StandardCharsets.UTF_8));
	}
}
