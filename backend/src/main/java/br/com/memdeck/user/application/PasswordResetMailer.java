package br.com.memdeck.user.application;

public interface PasswordResetMailer {

	void sendCode(String email, String code);
}
