package br.com.memdeck.flashcard.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.OwnedDeckFinder;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.flashcard.domain.InvalidFlashcardException;

class UpdateFlashcardUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final UpdateFlashcardUseCase useCase = new UpdateFlashcardUseCaseImpl(
			new OwnedFlashcardFinder(new OwnedDeckFinder(decks), flashcards), flashcards);

	@Test
	void theOwnerChangesTheText() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Flashcard card = flashcards.save(Flashcard.create(deck.getId(), "Femur", "Bone"));

		Flashcard changed = useCase.execute(new UpdateFlashcardCommand(OWNER, deck.getId(), card.getId(), "Tibia", "Shin bone"));

		assertThat(changed.getId()).isEqualTo(card.getId());
		assertThat(flashcards.findById(card.getId()).orElseThrow().getFront()).isEqualTo("Tibia");
		assertThat(flashcards.size()).isEqualTo(1);
	}

	@Test
	void aStrangerCannotChangeIt() {
		Deck hidden = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck open = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));
		Flashcard hiddenCard = flashcards.save(Flashcard.create(hidden.getId(), "Femur", "Bone"));
		Flashcard openCard = flashcards.save(Flashcard.create(open.getId(), "Hello", "Olá"));

		assertThatThrownBy(() -> useCase.execute(new UpdateFlashcardCommand(STRANGER, hidden.getId(), hiddenCard.getId(), "x", "y")))
				.isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> useCase.execute(new UpdateFlashcardCommand(STRANGER, open.getId(), openCard.getId(), "x", "y")))
				.isInstanceOf(NotDeckOwnerException.class);
		assertThat(flashcards.findById(openCard.getId()).orElseThrow().getFront()).isEqualTo("Hello");
	}

	@Test
	void aFlashcardOfAnotherDeckIsNotFoundThroughThisDeck() {
		Deck mine = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck other = decks.save(Deck.create(OWNER, "English", null, Visibility.PRIVATE));
		Flashcard card = flashcards.save(Flashcard.create(other.getId(), "Hello", "Olá"));

		assertThatThrownBy(() -> useCase.execute(new UpdateFlashcardCommand(OWNER, mine.getId(), card.getId(), "x", "y")))
				.isInstanceOf(FlashcardNotFoundException.class);
	}

	@Test
	void keepsTheFlashcardUntouchedWhenTheChangeIsInvalid() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Flashcard card = flashcards.save(Flashcard.create(deck.getId(), "Femur", "Bone"));

		assertThatThrownBy(() -> useCase.execute(new UpdateFlashcardCommand(OWNER, deck.getId(), card.getId(), " ", "y")))
				.isInstanceOf(InvalidFlashcardException.class);
		assertThat(flashcards.findById(card.getId()).orElseThrow().getFront()).isEqualTo("Femur");
	}
}
