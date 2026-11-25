package br.com.memdeck.user.application;

import java.util.UUID;

public interface PasswordResetCodes {

	String generate();

	String hash(UUID userId, String code);

	boolean matches(UUID userId, String code, String codeHash);
}
