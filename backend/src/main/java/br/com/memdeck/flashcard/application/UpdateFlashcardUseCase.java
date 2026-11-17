package br.com.memdeck.flashcard.application;

import br.com.memdeck.flashcard.domain.Flashcard;

public interface UpdateFlashcardUseCase {

	Flashcard execute(UpdateFlashcardCommand command);
}
