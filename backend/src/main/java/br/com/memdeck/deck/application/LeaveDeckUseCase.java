package br.com.memdeck.deck.application;

import java.util.UUID;

public interface LeaveDeckUseCase {

	void execute(UUID deckId, UUID requesterId);
}
