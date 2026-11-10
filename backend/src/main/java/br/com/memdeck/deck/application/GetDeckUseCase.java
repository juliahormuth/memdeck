package br.com.memdeck.deck.application;

import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

/** Input port: retrieves an existing deck. */
public interface GetDeckUseCase {

	Deck execute(UUID id);
}
