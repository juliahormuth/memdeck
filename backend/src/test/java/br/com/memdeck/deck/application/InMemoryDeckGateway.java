package br.com.memdeck.deck.application;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.Visibility;

public class InMemoryDeckGateway implements DeckGateway {

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
	public void deleteById(UUID id) {
		store.remove(id);
	}

	@Override
	public List<Deck> findByOwnerNewestFirst(UUID ownerId) {
		return store.values().stream()
				.filter(deck -> deck.isOwnedBy(ownerId))
				.sorted(Comparator.comparing(Deck::getCreatedAt).reversed())
				.toList();
	}

	@Override
	public List<Deck> findAllById(Collection<UUID> ids) {
		return ids.stream().map(store::get).filter(java.util.Objects::nonNull).toList();
	}

	@Override
	public List<Deck> searchPublicNewestFirst(UUID excludedOwnerId, String term, int limit) {
		String lowered = term.toLowerCase(Locale.ROOT);
		return store.values().stream()
				.filter(deck -> deck.getVisibility() == Visibility.PUBLIC && !deck.isOwnedBy(excludedOwnerId))
				.filter(deck -> deck.getName().toLowerCase(Locale.ROOT).contains(lowered)
						|| (deck.getDescription() != null && deck.getDescription().toLowerCase(Locale.ROOT).contains(lowered)))
				.sorted(Comparator.comparing(Deck::getCreatedAt).reversed())
				.limit(limit)
				.toList();
	}

	int size() {
		return store.size();
	}
}
