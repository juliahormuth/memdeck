package br.com.memdeck.friendship.application;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record FriendView(UUID userId, String name, Avatar avatar, Instant since) {
}
