package br.com.memdeck.user.domain;

public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException() {
		super("Email is already registered");
	}
}
