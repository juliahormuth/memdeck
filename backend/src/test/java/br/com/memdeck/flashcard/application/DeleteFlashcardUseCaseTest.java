package br.com.memdeck.flashcard.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.InMemoryDeckMemberGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckEditorException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;

class DeleteFlashcardUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final DeleteFlashcardUseCase useCase = new DeleteFlashcardUseCaseImpl(
			new EditableFlashcardFinder(new DeckAccessFinder(decks, new InMemoryDeckMemberGateway()), flashcards), flashcards);

	@Test
	void theOwnerDeletesTheFlashcard() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Flashcard card = flashcards.save(Flashcard.create(deck.getId(), "Femur", "Bone"));

		useCase.execute(deck.getId(), card.getId(), OWNER);

		assertThat(flashcards.size()).isZero();
	}

	@Test
	void aStrangerCannotDeleteIt() {
		Deck hidden = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck open = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));
		Flashcard hiddenCard = flashcards.save(Flashcard.create(hidden.getId(), "Femur", "Bone"));
		Flashcard openCard = flashcards.save(Flashcard.create(open.getId(), "Hello", "Olá"));

		assertThatThrownBy(() -> useCase.execute(hidden.getId(), hiddenCard.getId(), STRANGER))
				.isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> useCase.execute(open.getId(), openCard.getId(), STRANGER))
				.isInstanceOf(NotDeckEditorException.class);
		assertThat(flashcards.size()).isEqualTo(2);
	}

	@Test
	void failsWhenTheFlashcardDoesNotExistInThatDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck other = decks.save(Deck.create(OWNER, "English", null, Visibility.PRIVATE));
		Flashcard card = flashcards.save(Flashcard.create(other.getId(), "Hello", "Olá"));

		assertThatThrownBy(() -> useCase.execute(deck.getId(), card.getId(), OWNER)).isInstanceOf(FlashcardNotFoundException.class);
		assertThatThrownBy(() -> useCase.execute(deck.getId(), UUID.randomUUID(), OWNER)).isInstanceOf(FlashcardNotFoundException.class);
		assertThat(flashcards.size()).isEqualTo(1);
	}
}
