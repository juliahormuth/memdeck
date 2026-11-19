package br.com.memdeck.friendship.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ListFriendsUseCaseImpl implements ListFriendsUseCase {

	private final UserGateway users;
	private final FriendshipGateway friendships;

	public ListFriendsUseCaseImpl(UserGateway users, FriendshipGateway friendships) {
		this.users = users;
		this.friendships = friendships;
	}

	@Override
	public List<FriendView> execute(UUID userId) {
		List<Friendship> accepted = friendships.findAcceptedInvolving(userId);
		Map<UUID, User> others = users.findAllById(accepted.stream().map(friendship -> friendship.otherUser(userId)).toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return accepted.stream()
				.map(friendship -> {
					User other = others.get(friendship.otherUser(userId));
					return new FriendView(other.getId(), other.getName(), other.getAvatar(), friendship.getRespondedAt());
				})
				.sorted(Comparator.comparing(FriendView::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}
}
