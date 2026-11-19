package br.com.memdeck.friendship.application;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record FriendRequestView(UUID id, Direction direction, UUID userId, String name, Avatar avatar,
		Instant createdAt) {

	public enum Direction {
		INCOMING,
		OUTGOING
	}
}
