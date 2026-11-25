package br.com.memdeck.user.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.PasswordHasher;

@Component
class Argon2PepperPasswordHasher implements PasswordHasher {

	private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
	private final Pepper pepper;

	Argon2PepperPasswordHasher(@Value("${memdeck.security.password-pepper}") String pepperBase64) {
		this.pepper = new Pepper(pepperBase64);
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(pepper.sign(rawPassword));
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return encoder.matches(pepper.sign(rawPassword), passwordHash);
	}
}
