package br.com.memdeck.deck.application;

import java.util.UUID;

public interface GetDeckUseCase {

	DeckView execute(UUID id, UUID requesterId);
}
