package br.com.memdeck.user.presentation;

import br.com.memdeck.user.application.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 128) String password) {

	RegisterUserCommand toCommand() {
		return new RegisterUserCommand(name, email, password);
	}

	@Override
	public String toString() {
		return "RegisterUserRequest[name=" + name + ", email=" + email + ", password=***]";
	}
}
