package br.com.memdeck.friendship.application;

import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record FoundUser(UUID userId, String name, String username, Avatar avatar, UserRelation relation) {
}
