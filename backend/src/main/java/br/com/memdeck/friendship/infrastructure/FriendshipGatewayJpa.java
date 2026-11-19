package br.com.memdeck.friendship.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.friendship.application.FriendshipGateway;
import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.FriendshipStatus;

@Component
class FriendshipGatewayJpa implements FriendshipGateway {

	private final FriendshipJpaRepository repository;

	FriendshipGatewayJpa(FriendshipJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Friendship save(Friendship friendship) {
		return toDomain(repository.save(new FriendshipJpaModel(friendship.getId(), friendship.getRequesterId(),
				friendship.getAddresseeId(), friendship.getStatus(), friendship.getCreatedAt(),
				friendship.getRespondedAt())));
	}

	@Override
	public Optional<Friendship> findById(UUID id) {
		return repository.findById(id).map(FriendshipGatewayJpa::toDomain);
	}

	@Override
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

	@Override
	public Optional<Friendship> findBetween(UUID userId, UUID otherUserId) {
		return repository.findBetween(userId, otherUserId).map(FriendshipGatewayJpa::toDomain);
	}

	@Override
	public List<Friendship> findPendingInvolving(UUID userId) {
		return repository.findInvolving(userId, FriendshipStatus.PENDING).stream()
				.map(FriendshipGatewayJpa::toDomain).toList();
	}

	@Override
	public List<Friendship> findAcceptedInvolving(UUID userId) {
		return repository.findInvolving(userId, FriendshipStatus.ACCEPTED).stream()
				.map(FriendshipGatewayJpa::toDomain).toList();
	}

	private static Friendship toDomain(FriendshipJpaModel model) {
		return Friendship.restore(model.getId(), model.getRequesterId(), model.getAddresseeId(), model.getStatus(),
				model.getCreatedAt(), model.getRespondedAt());
	}
}
