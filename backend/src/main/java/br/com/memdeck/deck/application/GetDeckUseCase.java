package br.com.memdeck.deck.application;

import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

public interface GetDeckUseCase {

	Deck execute(UUID id, UUID requesterId);
}
