package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.InvalidDeckException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;

class UpdateDeckUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final UpdateDeckUseCase useCase = new UpdateDeckUseCaseImpl(new DeckAccessFinder(gateway, new InMemoryDeckMemberGateway()), gateway);

	@Test
	void theOwnerChangesTheDeck() {
		Deck deck = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		Deck changed = useCase.execute(new UpdateDeckCommand(OWNER, deck.getId(), "Human anatomy", "Bones", Visibility.PUBLIC));

		assertThat(gateway.findById(deck.getId()).orElseThrow()).isSameAs(changed);
		assertThat(changed.getName()).isEqualTo("Human anatomy");
		assertThat(changed.getVisibility()).isEqualTo(Visibility.PUBLIC);
	}

	@Test
	void aStrangerCannotSeeThatAPrivateDeckExists() {
		Deck deck = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(new UpdateDeckCommand(STRANGER, deck.getId(), "Hacked", null, Visibility.PUBLIC)))
				.isInstanceOf(DeckNotFoundException.class);
		assertThat(gateway.findById(deck.getId()).orElseThrow().getName()).isEqualTo("Anatomy");
	}

	@Test
	void aStrangerCanSeeButNotChangeAPublicDeck() {
		Deck deck = gateway.save(Deck.create(OWNER, "English", null, Visibility.PUBLIC));

		assertThatThrownBy(() -> useCase.execute(new UpdateDeckCommand(STRANGER, deck.getId(), "Hacked", null, Visibility.PUBLIC)))
				.isInstanceOf(NotDeckOwnerException.class);
		assertThat(gateway.findById(deck.getId()).orElseThrow().getName()).isEqualTo("English");
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(() -> useCase.execute(new UpdateDeckCommand(OWNER, UUID.randomUUID(), "x", null, Visibility.PUBLIC)))
				.isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void keepsTheDeckUntouchedWhenTheChangeIsInvalid() {
		Deck deck = gateway.save(Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(new UpdateDeckCommand(OWNER, deck.getId(), " ", null, Visibility.PUBLIC)))
				.isInstanceOf(InvalidDeckException.class);
		assertThat(gateway.findById(deck.getId()).orElseThrow().getName()).isEqualTo("Anatomy");
	}
}
