package br.com.memdeck.user.application;

public record AuthenticateUserCommand(String email, String password, String origin) {

	@Override
	public String toString() {
		return "AuthenticateUserCommand[email=" + email + ", password=***, origin=" + origin + "]";
	}
}
