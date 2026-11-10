package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Deck;

/** Input port: creates a new deck. */
public interface CreateDeckUseCase {

	Deck execute(CreateDeckCommand command);
}
