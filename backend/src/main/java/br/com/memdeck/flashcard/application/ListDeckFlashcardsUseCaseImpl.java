package br.com.memdeck.flashcard.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional(readOnly = true)
public class ListDeckFlashcardsUseCaseImpl implements ListDeckFlashcardsUseCase {

	private final DeckGateway decks;
	private final FlashcardGateway flashcards;

	public ListDeckFlashcardsUseCaseImpl(DeckGateway decks, FlashcardGateway flashcards) {
		this.decks = decks;
		this.flashcards = flashcards;
	}

	@Override
	public List<Flashcard> execute(UUID deckId, UUID requesterId) {
		decks.findById(deckId)
				.filter(found -> found.canBeViewedBy(requesterId))
				.orElseThrow(() -> new DeckNotFoundException(deckId));
		return flashcards.findByDeckNewestFirst(deckId);
	}
}
