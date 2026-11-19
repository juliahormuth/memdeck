package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckEditorException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;

class DeckAccessFinderTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID EDITOR = UUID.randomUUID();
	private static final UUID READER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final DeckAccessFinder finder = new DeckAccessFinder(decks, members);

	private Deck deckWithTeam(Visibility visibility) {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, visibility));
		members.save(DeckMember.join(deck.getId(), EDITOR, DeckRole.EDITOR));
		members.save(DeckMember.join(deck.getId(), READER, DeckRole.READER));
		return deck;
	}

	@Test
	void everyParticipantSeesAPrivateDeckWithTheirRole() {
		Deck deck = deckWithTeam(Visibility.PRIVATE);

		assertThat(finder.viewable(deck.getId(), OWNER).role()).isEqualTo(DeckRole.OWNER);
		assertThat(finder.viewable(deck.getId(), EDITOR).role()).isEqualTo(DeckRole.EDITOR);
		assertThat(finder.viewable(deck.getId(), READER).role()).isEqualTo(DeckRole.READER);
	}

	@Test
	void aStrangerDoesNotSeeAPrivateDeckAndGetsTheSameErrorAsForAMissingOne() {
		Deck deck = deckWithTeam(Visibility.PRIVATE);

		assertThatThrownBy(() -> finder.viewable(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> finder.viewable(UUID.randomUUID(), OWNER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void aStrangerSeesAPublicDeckWithoutARole() {
		Deck deck = deckWithTeam(Visibility.PUBLIC);

		assertThat(finder.viewable(deck.getId(), STRANGER).role()).isNull();
	}

	@Test
	void ownerAndEditorMayChangeFlashcardsButReaderAndStrangerMayNot() {
		Deck deck = deckWithTeam(Visibility.PUBLIC);

		assertThat(finder.editable(deck.getId(), OWNER).deck()).isSameAs(deck);
		assertThat(finder.editable(deck.getId(), EDITOR).deck()).isSameAs(deck);
		assertThatThrownBy(() -> finder.editable(deck.getId(), READER)).isInstanceOf(NotDeckEditorException.class);
		assertThatThrownBy(() -> finder.editable(deck.getId(), STRANGER)).isInstanceOf(NotDeckEditorException.class);
	}

	@Test
	void aPrivateDeckStaysHiddenEvenWhenTheRightIsMissing() {
		Deck deck = deckWithTeam(Visibility.PRIVATE);

		assertThatThrownBy(() -> finder.editable(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> finder.owned(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void onlyTheOwnerOwnsTheDeck() {
		Deck deck = deckWithTeam(Visibility.PRIVATE);

		assertThat(finder.owned(deck.getId(), OWNER).deck()).isSameAs(deck);
		assertThatThrownBy(() -> finder.owned(deck.getId(), EDITOR)).isInstanceOf(NotDeckOwnerException.class);
		assertThatThrownBy(() -> finder.owned(deck.getId(), READER)).isInstanceOf(NotDeckOwnerException.class);
	}
}
