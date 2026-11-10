package br.com.memdeck.deck.application;

import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

public interface DeckGateway {

	Deck save(Deck deck);

	Optional<Deck> findById(UUID id);
}
