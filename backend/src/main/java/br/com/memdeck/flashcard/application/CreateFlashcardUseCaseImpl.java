package br.com.memdeck.flashcard.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.OwnedDeckFinder;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional
public class CreateFlashcardUseCaseImpl implements CreateFlashcardUseCase {

	private final OwnedDeckFinder ownedDeck;
	private final FlashcardGateway flashcards;

	public CreateFlashcardUseCaseImpl(OwnedDeckFinder ownedDeck, FlashcardGateway flashcards) {
		this.ownedDeck = ownedDeck;
		this.flashcards = flashcards;
	}

	@Override
	public Flashcard execute(CreateFlashcardCommand command) {
		Deck deck = ownedDeck.find(command.deckId(), command.requesterId());
		return flashcards.save(Flashcard.create(deck.getId(), command.front(), command.back()));
	}
}
