package br.com.memdeck.flashcard.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.flashcard.domain.Flashcard;

class InMemoryFlashcardGateway implements FlashcardGateway {

	private final List<Flashcard> store = new ArrayList<>();

	@Override
	public Flashcard save(Flashcard flashcard) {
		store.removeIf(existing -> existing.getId().equals(flashcard.getId()));
		store.add(flashcard);
		return flashcard;
	}

	@Override
	public Optional<Flashcard> findById(UUID id) {
		return store.stream().filter(flashcard -> flashcard.getId().equals(id)).findFirst();
	}

	@Override
	public void deleteById(UUID id) {
		store.removeIf(flashcard -> flashcard.getId().equals(id));
	}

	@Override
	public List<Flashcard> findByDeckNewestFirst(UUID deckId) {
		return store.stream()
				.filter(flashcard -> flashcard.getDeckId().equals(deckId))
				.sorted(Comparator.comparing(Flashcard::getCreatedAt).reversed())
				.toList();
	}

	int size() {
		return store.size();
	}
}
