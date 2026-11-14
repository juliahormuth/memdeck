package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.Visibility;

class ListMyDecksUseCaseTest {

	private static final UUID ME = UUID.randomUUID();
	private static final UUID SOMEONE_ELSE = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final ListMyDecksUseCase useCase = new ListMyDecksUseCaseImpl(gateway);

	private Deck deck(UUID owner, String name, String createdAt) {
		return gateway.save(Deck.restore(UUID.randomUUID(), owner, name, null, Visibility.PRIVATE, Instant.parse(createdAt)));
	}

	@Test
	void listsOnlyMyDecksNewestFirst() {
		deck(ME, "Oldest", "2026-01-01T10:00:00Z");
		deck(SOMEONE_ELSE, "Not mine", "2026-01-02T10:00:00Z");
		deck(ME, "Newest", "2026-01-03T10:00:00Z");

		assertThat(useCase.execute(ME)).extracting(Deck::getName).containsExactly("Newest", "Oldest");
	}

	@Test
	void returnsAnEmptyListWhenIHaveNoDecks() {
		deck(SOMEONE_ELSE, "Not mine", "2026-01-02T10:00:00Z");

		assertThat(useCase.execute(ME)).isEmpty();
	}
}
