package br.com.memdeck.deck.application;

import java.util.UUID;

public interface JoinDeckUseCase {

	DeckView execute(UUID deckId, UUID requesterId);
}
