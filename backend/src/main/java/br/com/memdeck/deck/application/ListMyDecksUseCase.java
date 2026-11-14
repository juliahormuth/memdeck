package br.com.memdeck.deck.application;

import java.util.List;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

public interface ListMyDecksUseCase {

	List<Deck> execute(UUID ownerId);
}
