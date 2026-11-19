package br.com.memdeck.flashcard.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.flashcard.domain.Flashcard;

@Service
@Transactional
public class CreateFlashcardUseCaseImpl implements CreateFlashcardUseCase {

	private final DeckAccessFinder access;
	private final FlashcardGateway flashcards;

	public CreateFlashcardUseCaseImpl(DeckAccessFinder access, FlashcardGateway flashcards) {
		this.access = access;
		this.flashcards = flashcards;
	}

	@Override
	public Flashcard execute(CreateFlashcardCommand command) {
		UUID deckId = access.editable(command.deckId(), command.requesterId()).deck().getId();
		return flashcards.save(Flashcard.create(deckId, command.front(), command.back()));
	}
}
