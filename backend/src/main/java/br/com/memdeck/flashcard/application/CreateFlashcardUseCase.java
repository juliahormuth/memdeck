package br.com.memdeck.flashcard.application;

import br.com.memdeck.flashcard.domain.Flashcard;

public interface CreateFlashcardUseCase {

	Flashcard execute(CreateFlashcardCommand command);
}
