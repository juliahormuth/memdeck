package br.com.memdeck.user.application;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.user.domain.User;

public interface UserGateway {

	User save(User user);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findById(UUID id);

	List<User> findAllById(Collection<UUID> ids);
}
