package br.com.memdeck.flashcard.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.domain.Flashcard;

class ListDeckFlashcardsUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final ListDeckFlashcardsUseCase useCase = new ListDeckFlashcardsUseCaseImpl(decks, flashcards);

	@Test
	void theOwnerListsOnlyTheFlashcardsOfThatDeckNewestFirst() throws InterruptedException {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck other = decks.save(Deck.create(OWNER, "English", null, Visibility.PRIVATE));
		Flashcard first = flashcards.save(Flashcard.create(deck.getId(), "Femur", "Thigh bone"));
		Thread.sleep(5);
		Flashcard second = flashcards.save(Flashcard.create(deck.getId(), "Tibia", "Shin bone"));
		flashcards.save(Flashcard.create(other.getId(), "Hello", "Olá"));

		assertThat(useCase.execute(deck.getId(), OWNER)).containsExactly(second, first);
	}

	@Test
	void aStrangerListsTheFlashcardsOfAPublicDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));
		Flashcard card = flashcards.save(Flashcard.create(deck.getId(), "Hello", "Olá"));

		assertThat(useCase.execute(deck.getId(), STRANGER)).containsExactly(card);
	}

	@Test
	void aStrangerCannotListTheFlashcardsOfAPrivateDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		flashcards.save(Flashcard.create(deck.getId(), "Femur", "Thigh bone"));

		assertThatThrownBy(() -> useCase.execute(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), OWNER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void anEmptyDeckHasNoFlashcards() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThat(useCase.execute(deck.getId(), OWNER)).isEqualTo(List.of());
	}
}
