package br.com.memdeck.flashcard.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteFlashcardUseCaseImpl implements DeleteFlashcardUseCase {

	private final OwnedFlashcardFinder ownedFlashcard;
	private final FlashcardGateway flashcards;

	public DeleteFlashcardUseCaseImpl(OwnedFlashcardFinder ownedFlashcard, FlashcardGateway flashcards) {
		this.ownedFlashcard = ownedFlashcard;
		this.flashcards = flashcards;
	}

	@Override
	public void execute(UUID deckId, UUID flashcardId, UUID requesterId) {
		flashcards.deleteById(ownedFlashcard.find(deckId, flashcardId, requesterId).getId());
	}
}
