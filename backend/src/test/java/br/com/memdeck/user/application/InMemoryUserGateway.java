package br.com.memdeck.user.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.memdeck.user.domain.User;

class InMemoryUserGateway implements UserGateway {

	private final Map<String, User> byEmail = new HashMap<>();

	@Override
	public User save(User user) {
		byEmail.put(user.getEmail(), user);
		return user;
	}

	@Override
	public boolean existsByEmail(String email) {
		return byEmail.containsKey(email);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return Optional.ofNullable(byEmail.get(email));
	}

	int size() {
		return byEmail.size();
	}
}
