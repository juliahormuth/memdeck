package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.InvalidDeckException;
import br.com.memdeck.deck.domain.Visibility;

class CreateDeckUseCaseTest {

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final CreateDeckUseCase useCase = new CreateDeckUseCaseImpl(gateway);

	@Test
	void savesTheCreatedDeck() {
		Deck deck = useCase.execute(new CreateDeckCommand("Anatomy", "Bones", Visibility.PRIVATE));

		assertThat(gateway.findById(deck.getId())).isPresent();
		assertThat(deck.getName()).isEqualTo("Anatomy");
	}

	@Test
	void doesNotSaveWhenTheDeckIsInvalid() {
		assertThatThrownBy(() -> useCase.execute(new CreateDeckCommand(" ", null, Visibility.PUBLIC)))
				.isInstanceOf(InvalidDeckException.class);

		assertThat(gateway.size()).isZero();
	}
}
