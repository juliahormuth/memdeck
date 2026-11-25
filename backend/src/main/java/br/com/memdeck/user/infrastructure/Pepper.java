package br.com.memdeck.user.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class Pepper {

	static final int MIN_BYTES = 32;

	private static final String HMAC = "HmacSHA256";

	private final byte[] key;

	Pepper(String base64) {
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(base64.strip());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("PASSWORD_PEPPER must be valid Base64");
		}
		if (decoded.length < MIN_BYTES) {
			throw new IllegalStateException("PASSWORD_PEPPER must have at least " + MIN_BYTES + " bytes");
		}
		this.key = decoded;
	}

	String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC);
			mac.init(new SecretKeySpec(key, HMAC));
			return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("HMAC-SHA256 is not available", exception);
		}
	}
}
