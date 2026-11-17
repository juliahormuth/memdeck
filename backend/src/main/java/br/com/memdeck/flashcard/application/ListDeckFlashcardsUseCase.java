package br.com.memdeck.flashcard.application;

import java.util.List;
import java.util.UUID;

import br.com.memdeck.flashcard.domain.Flashcard;

public interface ListDeckFlashcardsUseCase {

	List<Flashcard> execute(UUID deckId, UUID requesterId);
}
