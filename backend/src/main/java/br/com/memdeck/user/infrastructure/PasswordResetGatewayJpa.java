package br.com.memdeck.user.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.PasswordResetGateway;
import br.com.memdeck.user.domain.PasswordReset;

@Component
class PasswordResetGatewayJpa implements PasswordResetGateway {

	private final PasswordResetJpaRepository repository;

	PasswordResetGatewayJpa(PasswordResetJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<PasswordReset> findForUpdate(UUID userId) {
		repository.lockUser(userId);
		return repository.findById(userId).map(PasswordResetJpaModel::toDomain);
	}

	@Override
	public void save(PasswordReset reset) {
		repository.save(PasswordResetJpaModel.from(reset));
	}

	@Override
	public void delete(UUID userId) {
		repository.deleteById(userId);
	}
}
