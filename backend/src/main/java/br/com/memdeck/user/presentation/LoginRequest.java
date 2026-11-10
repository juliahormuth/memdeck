package br.com.memdeck.user.presentation;

import br.com.memdeck.user.application.AuthenticateUserCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank @Size(max = 254) String email,
		@NotBlank @Size(max = 128) String password) {

	AuthenticateUserCommand toCommand(String origin) {
		return new AuthenticateUserCommand(email, password, origin);
	}

	@Override
	public String toString() {
		return "LoginRequest[email=" + email + ", password=***]";
	}
}
