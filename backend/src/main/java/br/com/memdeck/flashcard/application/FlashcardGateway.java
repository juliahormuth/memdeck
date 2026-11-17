package br.com.memdeck.flashcard.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.flashcard.domain.Flashcard;

public interface FlashcardGateway {

	Flashcard save(Flashcard flashcard);

	Optional<Flashcard> findById(UUID id);

	void deleteById(UUID id);

	List<Flashcard> findByDeckNewestFirst(UUID deckId);
}
