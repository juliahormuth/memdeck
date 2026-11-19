package br.com.memdeck.friendship.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.FriendshipNotFoundException;

@Service
@Transactional
public class RemoveFriendUseCaseImpl implements RemoveFriendUseCase {

	private final FriendshipGateway friendships;

	public RemoveFriendUseCaseImpl(FriendshipGateway friendships) {
		this.friendships = friendships;
	}

	@Override
	public void execute(UUID userId, UUID friendId) {
		Friendship friendship = friendships.findBetween(userId, friendId)
				.filter(found -> !found.isPending())
				.orElseThrow(() -> new FriendshipNotFoundException(friendId));
		friendships.deleteById(friendship.getId());
	}
}
