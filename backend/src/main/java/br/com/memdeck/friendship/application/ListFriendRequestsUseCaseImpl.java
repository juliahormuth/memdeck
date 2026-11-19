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
public class ListFriendRequestsUseCaseImpl implements ListFriendRequestsUseCase {

	private final UserGateway users;
	private final FriendshipGateway friendships;

	public ListFriendRequestsUseCaseImpl(UserGateway users, FriendshipGateway friendships) {
		this.users = users;
		this.friendships = friendships;
	}

	@Override
	public List<FriendRequestView> execute(UUID userId) {
		List<Friendship> pending = friendships.findPendingInvolving(userId);
		Map<UUID, User> others = users.findAllById(pending.stream().map(request -> request.otherUser(userId)).toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return pending.stream()
				.sorted(Comparator.comparing(Friendship::getCreatedAt).reversed())
				.map(request -> {
					User other = others.get(request.otherUser(userId));
					FriendRequestView.Direction direction = request.getAddresseeId().equals(userId)
							? FriendRequestView.Direction.INCOMING
							: FriendRequestView.Direction.OUTGOING;
					return new FriendRequestView(request.getId(), direction, other.getId(), other.getName(),
							other.getAvatar(), request.getCreatedAt());
				})
				.toList();
	}
}
