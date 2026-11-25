package br.com.memdeck.user.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.user.domain.PasswordReset;

class InMemoryPasswordResetGateway implements PasswordResetGateway {

	final Map<UUID, PasswordReset> stored = new HashMap<>();

	@Override
	public Optional<PasswordReset> findForUpdate(UUID userId) {
		return Optional.ofNullable(stored.get(userId));
	}

	@Override
	public void save(PasswordReset reset) {
		stored.put(reset.userId(), reset);
	}

	@Override
	public void delete(UUID userId) {
		stored.remove(userId);
	}
}
