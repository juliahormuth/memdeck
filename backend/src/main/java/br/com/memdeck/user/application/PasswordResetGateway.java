package br.com.memdeck.user.application;

import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.user.domain.PasswordReset;

public interface PasswordResetGateway {

	// serializes concurrent requests of the same user, so parallel guesses cannot skip the attempt limit
	Optional<PasswordReset> findForUpdate(UUID userId);

	void save(PasswordReset reset);

	void delete(UUID userId);
}
