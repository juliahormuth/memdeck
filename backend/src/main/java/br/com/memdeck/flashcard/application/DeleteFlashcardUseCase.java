package br.com.memdeck.flashcard.application;

import java.util.UUID;

public interface DeleteFlashcardUseCase {

	void execute(UUID deckId, UUID flashcardId, UUID requesterId);
}
