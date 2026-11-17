package br.com.memdeck.deck.application;

import java.util.UUID;

public interface DeleteDeckUseCase {

	void execute(UUID deckId, UUID requesterId);
}
