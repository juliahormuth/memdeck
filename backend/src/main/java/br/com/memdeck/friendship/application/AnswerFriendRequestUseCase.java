package br.com.memdeck.friendship.application;

import java.util.UUID;

public interface AnswerFriendRequestUseCase {

	void accept(UUID requestId, UUID userId);

	void decline(UUID requestId, UUID userId);

	void cancel(UUID requestId, UUID userId);
}
