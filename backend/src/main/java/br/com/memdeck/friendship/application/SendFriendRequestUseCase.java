package br.com.memdeck.friendship.application;

import java.util.UUID;

public interface SendFriendRequestUseCase {

	void execute(UUID requesterId, String username);
}
