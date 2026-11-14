package br.com.memdeck.deck.application;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;

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

	@Override
	public List<Deck> findByOwnerNewestFirst(UUID ownerId) {
		return store.values().stream()
				.filter(deck -> deck.isOwnedBy(ownerId))
				.sorted(Comparator.comparing(Deck::getCreatedAt).reversed())
				.toList();
	}

	int size() {
		return store.size();
	}
}
