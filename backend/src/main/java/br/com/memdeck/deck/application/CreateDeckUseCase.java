package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Deck;

public interface CreateDeckUseCase {

	Deck execute(CreateDeckCommand command);
}
