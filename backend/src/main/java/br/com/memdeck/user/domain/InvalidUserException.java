package br.com.memdeck.user.domain;

public class InvalidUserException extends RuntimeException {

	public InvalidUserException(String message) {
		super(message);
	}
}
