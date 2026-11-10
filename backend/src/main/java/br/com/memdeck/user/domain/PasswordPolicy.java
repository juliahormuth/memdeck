package br.com.memdeck.user.domain;

public final class PasswordPolicy {

	public static final int MIN_LENGTH = 8;
	public static final int MAX_LENGTH = 128;

	private PasswordPolicy() {
	}

	public static void validate(String rawPassword) {
		if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
			throw new InvalidUserException("Password must have at least " + MIN_LENGTH + " characters");
		}
		if (rawPassword.length() > MAX_LENGTH) {
			throw new InvalidUserException("Password must have at most " + MAX_LENGTH + " characters");
		}
	}
}
