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
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.InvalidFlashcardException;
import br.com.memdeck.deck.domain.NotDeckEditorException;

class CreateFlashcardUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final CreateFlashcardUseCase useCase = new CreateFlashcardUseCaseImpl(new DeckAccessFinder(decks, new InMemoryDeckMemberGateway()), flashcards);

	@Test
	void theOwnerAddsAFlashcardToTheirDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		Flashcard flashcard = useCase.execute(new CreateFlashcardCommand(OWNER, deck.getId(), "Femur", "Thigh bone"));

		assertThat(flashcard.getDeckId()).isEqualTo(deck.getId());
		assertThat(flashcards.size()).isEqualTo(1);
	}

	@Test
	void aStrangerCannotSeeThatAPrivateDeckExists() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(new CreateFlashcardCommand(STRANGER, deck.getId(), "Femur", "Bone")))
				.isInstanceOf(DeckNotFoundException.class);
		assertThat(flashcards.size()).isZero();
	}

	@Test
	void aStrangerCanSeeButNotChangeAPublicDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		assertThatThrownBy(() -> useCase.execute(new CreateFlashcardCommand(STRANGER, deck.getId(), "Hello", "Olá")))
				.isInstanceOf(NotDeckEditorException.class);
		assertThat(flashcards.size()).isZero();
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(
				() -> useCase.execute(new CreateFlashcardCommand(OWNER, UUID.randomUUID(), "Femur", "Bone")))
				.isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void doesNotSaveWhenTheFlashcardIsInvalid() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(new CreateFlashcardCommand(OWNER, deck.getId(), " ", "Bone")))
				.isInstanceOf(InvalidFlashcardException.class);
		assertThat(flashcards.size()).isZero();
	}
}
