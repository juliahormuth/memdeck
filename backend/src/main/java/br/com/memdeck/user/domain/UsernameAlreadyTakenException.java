package br.com.memdeck.user.domain;

public class UsernameAlreadyTakenException extends RuntimeException {

	public UsernameAlreadyTakenException() {
		super("Username is already taken");
	}
}
