package br.com.memdeck.user.application;

public record RegisterUserCommand(String name, String username, String email, String password) {

	@Override
	public String toString() {
		return "RegisterUserCommand[name=" + name + ", username=" + username + ", email=" + email + ", password=***]";
	}
}
