package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.Visibility;

class GetDeckUseCaseTest {

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final GetDeckUseCase useCase = new GetDeckUseCase(gateway);

	@Test
	void returnsAnExistingDeck() {
		Deck saved = gateway.save(Deck.create("English", null, Visibility.PUBLIC));

		assertThat(useCase.execute(saved.getId())).isSameAs(saved);
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		UUID unknown = UUID.randomUUID();

		assertThatThrownBy(() -> useCase.execute(unknown)).isInstanceOf(DeckNotFoundException.class);
	}
}
