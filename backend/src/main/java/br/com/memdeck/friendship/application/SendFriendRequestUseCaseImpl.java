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

	// Unknown e-mails and existing friendships or requests end silently, so the answer never reveals who has an account.
	@Override
	public void execute(UUID requesterId, String email) {
		User addressee = users.findByEmail(normalized(email)).orElse(null);
		if (addressee == null) {
			return;
		}
		Friendship request = Friendship.request(requesterId, addressee.getId());
		if (friendships.findBetween(requesterId, addressee.getId()).isPresent()) {
			return;
		}
		friendships.save(request);
	}

	private static String normalized(String email) {
		try {
			return User.normalizeEmail(email);
		} catch (InvalidUserException exception) {
			throw new InvalidFriendRequestException("The e-mail is not valid");
		}
	}
}
