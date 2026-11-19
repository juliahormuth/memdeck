package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.AlreadyDeckMemberException;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.Visibility;

class JoinDeckUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final JoinDeckUseCase useCase = new JoinDeckUseCaseImpl(new DeckAccessFinder(decks, members), members);

	@Test
	void aUserJoinsAPublicDeckAsAReader() {
		Deck deck = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		DeckView view = useCase.execute(deck.getId(), STRANGER);

		assertThat(view.role()).isEqualTo(DeckRole.READER);
		assertThat(members.findByDeckAndUser(deck.getId(), STRANGER)).isPresent();
	}

	@Test
	void joiningAPrivateDeckOfSomeoneElseLooksLikeAMissingDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
		assertThat(members.size()).isZero();
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void theOwnerCannotJoinTheirOwnDeck() {
		Deck deck = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		assertThatThrownBy(() -> useCase.execute(deck.getId(), OWNER)).isInstanceOf(AlreadyDeckMemberException.class);
		assertThat(members.size()).isZero();
	}

	@Test
	void joiningTwiceFailsAndKeepsASingleMembership() {
		Deck deck = decks.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));
		useCase.execute(deck.getId(), STRANGER);

		assertThatThrownBy(() -> useCase.execute(deck.getId(), STRANGER)).isInstanceOf(AlreadyDeckMemberException.class);
		assertThat(members.size()).isEqualTo(1);
	}
}
