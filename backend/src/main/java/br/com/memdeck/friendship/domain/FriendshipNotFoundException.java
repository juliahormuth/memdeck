package br.com.memdeck.friendship.domain;

import java.util.UUID;

public class FriendshipNotFoundException extends RuntimeException {

	public FriendshipNotFoundException(UUID id) {
		super("Friendship or friend request " + id + " not found");
	}
}
