package br.com.memdeck.flashcard.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional
public class UpdateFlashcardUseCaseImpl implements UpdateFlashcardUseCase {

	private final EditableFlashcardFinder editableFlashcard;
	private final FlashcardGateway flashcards;

	public UpdateFlashcardUseCaseImpl(EditableFlashcardFinder editableFlashcard, FlashcardGateway flashcards) {
		this.editableFlashcard = editableFlashcard;
		this.flashcards = flashcards;
	}

	@Override
	public Flashcard execute(UpdateFlashcardCommand command) {
		Flashcard flashcard = editableFlashcard.find(command.deckId(), command.flashcardId(), command.requesterId());
		return flashcards.save(flashcard.withChanges(command.front(), command.back()));
	}
}
