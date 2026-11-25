package br.com.memdeck.user.application;

public record ConfirmPasswordResetCommand(String email, String code, String newPassword) {

	@Override
	public String toString() {
		return "ConfirmPasswordResetCommand[email=" + email + ", code=***, newPassword=***]";
	}
}
