package br.com.memdeck.flashcard.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.InMemoryDeckMemberGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckEditorException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.domain.Flashcard;

class EditorPermissionsTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID EDITOR = UUID.randomUUID();
	private static final UUID READER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final DeckAccessFinder access = new DeckAccessFinder(decks, members);
	private final CreateFlashcardUseCase create = new CreateFlashcardUseCaseImpl(access, flashcards);
	private final UpdateFlashcardUseCase update = new UpdateFlashcardUseCaseImpl(
			new EditableFlashcardFinder(access, flashcards), flashcards);
	private final DeleteFlashcardUseCase delete = new DeleteFlashcardUseCaseImpl(
			new EditableFlashcardFinder(access, flashcards), flashcards);
	private final ListDeckFlashcardsUseCase list = new ListDeckFlashcardsUseCaseImpl(access, flashcards);

	private final Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

	EditorPermissionsTest() {
		members.save(DeckMember.join(deck.getId(), EDITOR, DeckRole.EDITOR));
		members.save(DeckMember.join(deck.getId(), READER, DeckRole.READER));
	}

	@Test
	void anEditorCreatesChangesAndDeletesFlashcardsOfAPrivateDeck() {
		Flashcard created = create.execute(new CreateFlashcardCommand(EDITOR, deck.getId(), "Femur", "Bone"));

		Flashcard changed = update.execute(
				new UpdateFlashcardCommand(EDITOR, deck.getId(), created.getId(), "Femur?", "Thigh bone"));
		assertThat(changed.getBack()).isEqualTo("Thigh bone");

		delete.execute(deck.getId(), created.getId(), EDITOR);
		assertThat(flashcards.size()).isZero();
	}

	@Test
	void aReaderListsButCannotChangeFlashcards() {
		Flashcard card = flashcards.save(Flashcard.create(deck.getId(), "Femur", "Bone"));

		assertThat(list.execute(deck.getId(), READER)).containsExactly(card);
		assertThatThrownBy(() -> create.execute(new CreateFlashcardCommand(READER, deck.getId(), "x", "y")))
				.isInstanceOf(NotDeckEditorException.class);
		assertThatThrownBy(() -> update.execute(new UpdateFlashcardCommand(READER, deck.getId(), card.getId(), "x", "y")))
				.isInstanceOf(NotDeckEditorException.class);
		assertThatThrownBy(() -> delete.execute(deck.getId(), card.getId(), READER))
				.isInstanceOf(NotDeckEditorException.class);
		assertThat(flashcards.size()).isEqualTo(1);
	}
}
