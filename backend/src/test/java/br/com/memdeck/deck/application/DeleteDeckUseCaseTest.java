package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;

class DeleteDeckUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final DeleteDeckUseCase useCase = new DeleteDeckUseCaseImpl(new DeckAccessFinder(gateway, new InMemoryDeckMemberGateway()), gateway);

	@Test
	void theOwnerDeletesTheDeck() {
		Deck deck = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		useCase.execute(deck.getId(), OWNER);

		assertThat(gateway.findById(deck.getId())).isEmpty();
	}

	@Test
	void aStrangerCannotDeleteAPrivateOrPublicDeck() {
		Deck hidden = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));
		Deck open = gateway.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		assertThatThrownBy(() -> useCase.execute(hidden.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> useCase.execute(open.getId(), STRANGER)).isInstanceOf(NotDeckOwnerException.class);
		assertThat(gateway.size()).isEqualTo(2);
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), OWNER)).isInstanceOf(DeckNotFoundException.class);
	}
}
