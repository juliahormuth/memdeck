package br.com.memdeck.deck.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

public interface DeckGateway {

	Deck save(Deck deck);

	Optional<Deck> findById(UUID id);

	List<Deck> findByOwnerNewestFirst(UUID ownerId);
}
