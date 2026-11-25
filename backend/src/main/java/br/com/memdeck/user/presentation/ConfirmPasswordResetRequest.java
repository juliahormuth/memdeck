package br.com.memdeck.user.presentation;

import br.com.memdeck.user.application.ConfirmPasswordResetCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordResetRequest(
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Pattern(regexp = "\\d{6}") String code,
		@NotBlank @Size(min = 8, max = 128) String newPassword) {

	ConfirmPasswordResetCommand toCommand() {
		return new ConfirmPasswordResetCommand(email, code, newPassword);
	}

	@Override
	public String toString() {
		return "ConfirmPasswordResetRequest[email=" + email + ", code=***, newPassword=***]";
	}
}
