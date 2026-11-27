package br.com.memdeck.friendship.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class SearchUserUseCaseImpl implements SearchUserUseCase {

	private final UserGateway users;
	private final FriendshipGateway friendships;
	private final SearchThrottle throttle;

	public SearchUserUseCaseImpl(UserGateway users, FriendshipGateway friendships, SearchThrottle throttle) {
		this.users = users;
		this.friendships = friendships;
		this.throttle = throttle;
	}

	@Override
	public Optional<FoundUser> execute(UUID requesterId, String username) {
		throttle.register(requesterId);
		return findByUsername(username).map(user -> new FoundUser(user.getId(), user.getName(), user.getUsername(),
				user.getAvatar(), relation(requesterId, user.getId())));
	}

	private Optional<User> findByUsername(String username) {
		try {
			return users.findByUsername(User.normalizeUsername(username));
		}
		catch (InvalidUserException exception) {
			return Optional.empty();
		}
	}

	private UserRelation relation(UUID requesterId, UUID otherId) {
		if (requesterId.equals(otherId)) {
			return UserRelation.SELF;
		}
		Optional<Friendship> found = friendships.findBetween(requesterId, otherId);
		if (found.isEmpty()) {
			return UserRelation.NONE;
		}
		if (!found.get().isPending()) {
			return UserRelation.FRIEND;
		}
		return found.get().getRequesterId().equals(requesterId) ? UserRelation.REQUEST_SENT : UserRelation.REQUEST_RECEIVED;
	}
}
