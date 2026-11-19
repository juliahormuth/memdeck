package br.com.memdeck.flashcard.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;

@Component
public class EditableFlashcardFinder {

	private final DeckAccessFinder access;
	private final FlashcardGateway flashcards;

	public EditableFlashcardFinder(DeckAccessFinder access, FlashcardGateway flashcards) {
		this.access = access;
		this.flashcards = flashcards;
	}

	public Flashcard find(UUID deckId, UUID flashcardId, UUID requesterId) {
		access.editable(deckId, requesterId);
		return flashcards.findById(flashcardId)
				.filter(found -> found.getDeckId().equals(deckId))
				.orElseThrow(() -> new FlashcardNotFoundException(flashcardId));
	}
}
