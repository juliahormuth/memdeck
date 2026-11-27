package br.com.memdeck.user.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.EmailAlreadyRegisteredException;
import br.com.memdeck.user.domain.User;
import br.com.memdeck.user.domain.UsernameAlreadyTakenException;

@Component
class UserGatewayJpa implements UserGateway {

	private final UserJpaRepository repository;

	UserGatewayJpa(UserJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public User save(User user) {
		try {
			return UserMapper.toDomain(repository.saveAndFlush(UserMapper.toModel(user)));
		} catch (DataIntegrityViolationException exception) {
			if (String.valueOf(exception.getMostSpecificCause().getMessage()).contains("uq_users_username")) {
				throw new UsernameAlreadyTakenException();
			}
			throw new EmailAlreadyRegisteredException();
		}
	}

	@Override
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(email);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmail(email).map(UserMapper::toDomain);
	}

	@Override
	public boolean existsByUsername(String username) {
		return repository.existsByUsername(username);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repository.findByUsername(username).map(UserMapper::toDomain);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return repository.findById(id).map(UserMapper::toDomain);
	}

	@Override
	public List<User> findAllById(Collection<UUID> ids) {
		return repository.findAllById(ids).stream().map(UserMapper::toDomain).toList();
	}
}
