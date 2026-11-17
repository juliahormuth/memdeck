package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Deck;

public interface UpdateDeckUseCase {

	Deck execute(UpdateDeckCommand command);
}
