package br.com.memdeck.user.application;

import java.util.Optional;

import br.com.memdeck.user.domain.User;

public interface UserGateway {

	User save(User user);

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);
}
