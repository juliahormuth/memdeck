package br.com.memdeck.friendship.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.friendship.domain.Friendship;

public interface FriendshipGateway {

	Friendship save(Friendship friendship);

	Optional<Friendship> findById(UUID id);

	void deleteById(UUID id);

	Optional<Friendship> findBetween(UUID userId, UUID otherUserId);

	List<Friendship> findPendingInvolving(UUID userId);

	List<Friendship> findAcceptedInvolving(UUID userId);
}
