package br.com.memdeck.user.domain;

public class InvalidPasswordResetCodeException extends RuntimeException {

	public InvalidPasswordResetCodeException() {
		super("Invalid or expired code");
	}
}
