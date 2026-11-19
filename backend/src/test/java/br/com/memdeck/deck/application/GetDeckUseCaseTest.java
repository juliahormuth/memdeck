package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.Visibility;

class GetDeckUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final GetDeckUseCase useCase = new GetDeckUseCaseImpl(new DeckAccessFinder(gateway, members));

	@Test
	void theOwnerReadsAPrivateDeck() {
		Deck saved = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		DeckView view = useCase.execute(saved.getId(), OWNER);

		assertThat(view.deck()).isSameAs(saved);
		assertThat(view.role()).isEqualTo(DeckRole.OWNER);
	}

	@Test
	void aStrangerCannotReadAPrivateDeckAndGetsTheSameErrorAsForAMissingOne() {
		Deck saved = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(saved.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void aStrangerReadsAPublicDeck() {
		Deck saved = gateway.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		DeckView view = useCase.execute(saved.getId(), STRANGER);

		assertThat(view.deck()).isSameAs(saved);
		assertThat(view.role()).isNull();
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		UUID unknown = UUID.randomUUID();

		assertThatThrownBy(() -> useCase.execute(unknown, OWNER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void aParticipantSeesTheirRole() {
		Deck saved = gateway.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));
		members.save(DeckMember.joinAsReader(saved.getId(), STRANGER));

		assertThat(useCase.execute(saved.getId(), STRANGER).role()).isEqualTo(DeckRole.READER);
	}
}
