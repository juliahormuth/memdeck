package br.com.memdeck.flashcard.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.application.OwnedDeckFinder;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;

@Component
public class OwnedFlashcardFinder {

	private final OwnedDeckFinder ownedDeck;
	private final FlashcardGateway flashcards;

	public OwnedFlashcardFinder(OwnedDeckFinder ownedDeck, FlashcardGateway flashcards) {
		this.ownedDeck = ownedDeck;
		this.flashcards = flashcards;
	}

	public Flashcard find(UUID deckId, UUID flashcardId, UUID requesterId) {
		ownedDeck.find(deckId, requesterId);
		return flashcards.findById(flashcardId)
				.filter(found -> found.getDeckId().equals(deckId))
				.orElseThrow(() -> new FlashcardNotFoundException(flashcardId));
	}
}
