package br.com.memdeck.user.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.PasswordHasher;

@Component
class Argon2PepperPasswordHasher implements PasswordHasher {

	static final int MIN_PEPPER_BYTES = 32;

	private static final String HMAC = "HmacSHA256";

	private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
	private final byte[] pepper;

	Argon2PepperPasswordHasher(@Value("${memdeck.security.password-pepper}") String pepperBase64) {
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(pepperBase64.strip());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("PASSWORD_PEPPER must be valid Base64");
		}
		if (decoded.length < MIN_PEPPER_BYTES) {
			throw new IllegalStateException("PASSWORD_PEPPER must have at least " + MIN_PEPPER_BYTES + " bytes");
		}
		this.pepper = decoded;
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(applyPepper(rawPassword));
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return encoder.matches(applyPepper(rawPassword), passwordHash);
	}

	private String applyPepper(String rawPassword) {
		try {
			Mac mac = Mac.getInstance(HMAC);
			mac.init(new SecretKeySpec(pepper, HMAC));
			byte[] digest = mac.doFinal(rawPassword.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(digest);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("HMAC-SHA256 is not available", exception);
		}
	}
}
