package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.Visibility;

class ListMyDecksUseCaseTest {

	private static final UUID ME = UUID.randomUUID();
	private static final UUID SOMEONE_ELSE = UUID.randomUUID();

	private final InMemoryDeckGateway gateway = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final ListMyDecksUseCase useCase = new ListMyDecksUseCaseImpl(gateway, members);

	private Deck deck(UUID owner, String name, String createdAt) {
		return gateway.save(Deck.restore(UUID.randomUUID(), owner, name, null, Visibility.PRIVATE, Instant.parse(createdAt)));
	}

	@Test
	void listsOnlyMyDecksNewestFirst() {
		deck(ME, "Oldest", "2026-01-01T10:00:00Z");
		deck(SOMEONE_ELSE, "Not mine", "2026-01-02T10:00:00Z");
		deck(ME, "Newest", "2026-01-03T10:00:00Z");

		assertThat(useCase.execute(ME)).extracting(view -> view.deck().getName()).containsExactly("Newest", "Oldest");
	}

	@Test
	void returnsAnEmptyListWhenIHaveNoDecks() {
		deck(SOMEONE_ELSE, "Not mine", "2026-01-02T10:00:00Z");

		assertThat(useCase.execute(ME)).isEmpty();
	}

	@Test
	void includesTheDecksIJoinedWithTheirRoleMixedByCreationDate() {
		deck(ME, "Mine", "2026-01-01T10:00:00Z");
		Deck joined = gateway.save(Deck.restore(UUID.randomUUID(), SOMEONE_ELSE, "Joined", null, Visibility.PUBLIC,
				Instant.parse("2026-01-02T10:00:00Z")));
		members.save(DeckMember.joinAsReader(joined.getId(), ME));

		List<DeckView> result = useCase.execute(ME);

		assertThat(result).extracting(view -> view.deck().getName()).containsExactly("Joined", "Mine");
		assertThat(result).extracting(DeckView::role).containsExactly(DeckRole.READER, DeckRole.OWNER);
	}

	@Test
	void hidesAJoinedDeckThatTheOwnerMadePrivateAgain() {
		Deck joined = gateway.save(Deck.restore(UUID.randomUUID(), SOMEONE_ELSE, "Joined", null, Visibility.PRIVATE,
				Instant.parse("2026-01-02T10:00:00Z")));
		members.save(DeckMember.joinAsReader(joined.getId(), ME));

		assertThat(useCase.execute(ME)).isEmpty();
	}
}
