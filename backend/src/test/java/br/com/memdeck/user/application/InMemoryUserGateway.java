package br.com.memdeck.user.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.user.domain.User;

public class InMemoryUserGateway implements UserGateway {

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

	@Override
	public List<User> findAllById(Collection<UUID> ids) {
		return byEmail.values().stream().filter(user -> ids.contains(user.getId())).toList();
	}

	int size() {
		return byEmail.size();
	}
}
