package br.com.memdeck.friendship.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.FriendshipNotFoundException;

@Service
@Transactional
public class AnswerFriendRequestUseCaseImpl implements AnswerFriendRequestUseCase {

	private final FriendshipGateway friendships;

	public AnswerFriendRequestUseCaseImpl(FriendshipGateway friendships) {
		this.friendships = friendships;
	}

	@Override
	public void accept(UUID requestId, UUID userId) {
		friendships.save(pendingFor(requestId, userId, true).accept());
	}

	@Override
	public void decline(UUID requestId, UUID userId) {
		friendships.deleteById(pendingFor(requestId, userId, true).getId());
	}

	@Override
	public void cancel(UUID requestId, UUID userId) {
		friendships.deleteById(pendingFor(requestId, userId, false).getId());
	}

	private Friendship pendingFor(UUID requestId, UUID userId, boolean asAddressee) {
		return friendships.findById(requestId)
				.filter(Friendship::isPending)
				.filter(request -> (asAddressee ? request.getAddresseeId() : request.getRequesterId()).equals(userId))
				.orElseThrow(() -> new FriendshipNotFoundException(requestId));
	}
}
