package br.com.memdeck.deck.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

/** Test double: lets the use cases be tested without Spring or a database. */
class InMemoryDeckGateway implements DeckGateway {

	private final Map<UUID, Deck> store = new HashMap<>();

	@Override
	public Deck save(Deck deck) {
		store.put(deck.getId(), deck);
		return deck;
	}

	@Override
	public Optional<Deck> findById(UUID id) {
		return Optional.ofNullable(store.get(id));
	}

	int size() {
		return store.size();
	}
}
