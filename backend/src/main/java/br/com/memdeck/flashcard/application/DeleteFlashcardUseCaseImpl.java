package br.com.memdeck.flashcard.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteFlashcardUseCaseImpl implements DeleteFlashcardUseCase {

	private final EditableFlashcardFinder editableFlashcard;
	private final FlashcardGateway flashcards;

	public DeleteFlashcardUseCaseImpl(EditableFlashcardFinder editableFlashcard, FlashcardGateway flashcards) {
		this.editableFlashcard = editableFlashcard;
		this.flashcards = flashcards;
	}

	@Override
	public void execute(UUID deckId, UUID flashcardId, UUID requesterId) {
		flashcards.deleteById(editableFlashcard.find(deckId, flashcardId, requesterId).getId());
	}
}
