package br.com.memdeck.flashcard.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional(readOnly = true)
public class ListDeckFlashcardsUseCaseImpl implements ListDeckFlashcardsUseCase {

	private final DeckAccessFinder access;
	private final FlashcardGateway flashcards;

	public ListDeckFlashcardsUseCaseImpl(DeckAccessFinder access, FlashcardGateway flashcards) {
		this.access = access;
		this.flashcards = flashcards;
	}

	@Override
	public List<Flashcard> execute(UUID deckId, UUID requesterId) {
		access.viewable(deckId, requesterId);
		return flashcards.findByDeckNewestFirst(deckId);
	}
}
