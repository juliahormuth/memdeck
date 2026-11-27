package br.com.memdeck.friendship.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.InvalidFriendRequestException;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

@Service
@Transactional
public class SendFriendRequestUseCaseImpl implements SendFriendRequestUseCase {

	private final UserGateway users;
	private final FriendshipGateway friendships;

	public SendFriendRequestUseCaseImpl(UserGateway users, FriendshipGateway friendships) {
		this.users = users;
		this.friendships = friendships;
	}

	// Unknown usernames end silently: only the throttled search tells whether a username exists.
	@Override
	public void execute(UUID requesterId, String username) {
		User addressee = users.findByUsername(normalized(username)).orElse(null);
		if (addressee == null) {
			return;
		}
		Friendship request = Friendship.request(requesterId, addressee.getId());
		if (friendships.findBetween(requesterId, addressee.getId()).isPresent()) {
			return;
		}
		friendships.save(request);
	}

	private static String normalized(String username) {
		try {
			return User.normalizeUsername(username);
		} catch (InvalidUserException exception) {
			throw new InvalidFriendRequestException("The username is not valid");
		}
	}
}
