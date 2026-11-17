package br.com.memdeck.flashcard.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional
public class UpdateFlashcardUseCaseImpl implements UpdateFlashcardUseCase {

	private final OwnedFlashcardFinder ownedFlashcard;
	private final FlashcardGateway flashcards;

	public UpdateFlashcardUseCaseImpl(OwnedFlashcardFinder ownedFlashcard, FlashcardGateway flashcards) {
		this.ownedFlashcard = ownedFlashcard;
		this.flashcards = flashcards;
	}

	@Override
	public Flashcard execute(UpdateFlashcardCommand command) {
		Flashcard flashcard = ownedFlashcard.find(command.deckId(), command.flashcardId(), command.requesterId());
		return flashcards.save(flashcard.withChanges(command.front(), command.back()));
	}
}
