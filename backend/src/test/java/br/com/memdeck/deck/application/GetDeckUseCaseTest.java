package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.Visibility;

class GetDeckUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final GetDeckUseCase useCase = new GetDeckUseCaseImpl(gateway);

	@Test
	void theOwnerReadsAPrivateDeck() {
		Deck saved = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThat(useCase.execute(saved.getId(), OWNER)).isSameAs(saved);
	}

	@Test
	void aStrangerCannotReadAPrivateDeckAndGetsTheSameErrorAsForAMissingOne() {
		Deck saved = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(saved.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void aStrangerReadsAPublicDeck() {
		Deck saved = gateway.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		assertThat(useCase.execute(saved.getId(), STRANGER)).isSameAs(saved);
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		UUID unknown = UUID.randomUUID();

		assertThatThrownBy(() -> useCase.execute(unknown, OWNER)).isInstanceOf(DeckNotFoundException.class);
	}
}
