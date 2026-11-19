package br.com.memdeck.friendship.application;

import java.util.UUID;

public interface RemoveFriendUseCase {

	void execute(UUID userId, UUID friendId);
}
