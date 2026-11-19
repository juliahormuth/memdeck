package br.com.memdeck.friendship.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.friendship.domain.Friendship;

public class InMemoryFriendshipGateway implements FriendshipGateway {

	private final List<Friendship> store = new ArrayList<>();

	@Override
	public Friendship save(Friendship friendship) {
		store.removeIf(existing -> existing.getId().equals(friendship.getId()));
		store.add(friendship);
		return friendship;
	}

	@Override
	public Optional<Friendship> findById(UUID id) {
		return store.stream().filter(friendship -> friendship.getId().equals(id)).findFirst();
	}

	@Override
	public void deleteById(UUID id) {
		store.removeIf(friendship -> friendship.getId().equals(id));
	}

	@Override
	public Optional<Friendship> findBetween(UUID userId, UUID otherUserId) {
		return store.stream().filter(f -> f.involves(userId) && f.involves(otherUserId)).findFirst();
	}

	@Override
	public List<Friendship> findPendingInvolving(UUID userId) {
		return store.stream().filter(f -> f.involves(userId) && f.isPending()).toList();
	}

	@Override
	public List<Friendship> findAcceptedInvolving(UUID userId) {
		return store.stream().filter(f -> f.involves(userId) && !f.isPending()).toList();
	}

	public int size() {
		return store.size();
	}
}
