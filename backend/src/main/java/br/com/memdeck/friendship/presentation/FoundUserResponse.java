package br.com.memdeck.friendship.presentation;

import java.util.UUID;

import br.com.memdeck.friendship.application.FoundUser;
import br.com.memdeck.friendship.application.UserRelation;
import br.com.memdeck.user.domain.Avatar;

public record FoundUserResponse(UUID userId, String name, String username, Avatar avatar, UserRelation relation) {

	static FoundUserResponse from(FoundUser user) {
		return new FoundUserResponse(user.userId(), user.name(), user.username(), user.avatar(), user.relation());
	}
}
