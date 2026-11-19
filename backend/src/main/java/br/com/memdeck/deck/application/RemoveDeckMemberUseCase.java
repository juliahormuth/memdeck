package br.com.memdeck.deck.application;

import java.util.UUID;

public interface RemoveDeckMemberUseCase {

	void execute(UUID deckId, UUID memberUserId, UUID requesterId);
}
