package br.com.memdeck.friendship.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.friendship.application.FriendView;
import br.com.memdeck.user.domain.Avatar;

public record FriendResponse(UUID userId, String name, Avatar avatar, Instant since) {

	static FriendResponse from(FriendView view) {
		return new FriendResponse(view.userId(), view.name(), view.avatar(), view.since());
	}
}
