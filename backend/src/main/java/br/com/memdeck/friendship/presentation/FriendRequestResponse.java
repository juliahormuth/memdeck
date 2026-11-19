package br.com.memdeck.friendship.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.friendship.application.FriendRequestView;
import br.com.memdeck.user.domain.Avatar;

public record FriendRequestResponse(UUID id, FriendRequestView.Direction direction, UUID userId, String name,
		Avatar avatar, Instant createdAt) {

	static FriendRequestResponse from(FriendRequestView view) {
		return new FriendRequestResponse(view.id(), view.direction(), view.userId(), view.name(), view.avatar(),
				view.createdAt());
	}
}
