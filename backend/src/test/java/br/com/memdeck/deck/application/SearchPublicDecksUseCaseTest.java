package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.Visibility;

class SearchPublicDecksUseCaseTest {

	private static final UUID ME = UUID.randomUUID();
	private static final UUID SOMEONE_ELSE = UUID.randomUUID();

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final SearchPublicDecksUseCase useCase = new SearchPublicDecksUseCaseImpl(decks, members);

	private Deck deck(UUID owner, String name, String description, Visibility visibility, String createdAt) {
		return decks.save(Deck.restore(UUID.randomUUID(), owner, name, description, visibility, Instant.parse(createdAt)));
	}

	@Test
	void listsOnlyPublicDecksOfOtherPeopleNewestFirst() {
		deck(SOMEONE_ELSE, "Older", null, Visibility.PUBLIC, "2026-01-01T10:00:00Z");
		deck(SOMEONE_ELSE, "Hidden", null, Visibility.PRIVATE, "2026-01-02T10:00:00Z");
		deck(ME, "Mine and public", null, Visibility.PUBLIC, "2026-01-03T10:00:00Z");
		deck(SOMEONE_ELSE, "Newer", null, Visibility.PUBLIC, "2026-01-04T10:00:00Z");

		assertThat(useCase.execute(ME, "")).extracting(view -> view.deck().getName()).containsExactly("Newer", "Older");
	}

	@Test
	void matchesTheTermInTheNameOrInTheDescriptionIgnoringCase() {
		deck(SOMEONE_ELSE, "English verbs", null, Visibility.PUBLIC, "2026-01-01T10:00:00Z");
		deck(SOMEONE_ELSE, "Vocabulary", "Everyday ENGLISH words", Visibility.PUBLIC, "2026-01-02T10:00:00Z");
		deck(SOMEONE_ELSE, "Anatomy", "Bones", Visibility.PUBLIC, "2026-01-03T10:00:00Z");

		assertThat(useCase.execute(ME, "english")).extracting(view -> view.deck().getName())
				.containsExactly("Vocabulary", "English verbs");
	}

	@Test
	void ignoresSpacesAroundTheTermAndTreatsNullAsEmpty() {
		deck(SOMEONE_ELSE, "English verbs", null, Visibility.PUBLIC, "2026-01-01T10:00:00Z");

		assertThat(useCase.execute(ME, "  verbs  ")).hasSize(1);
		assertThat(useCase.execute(ME, null)).hasSize(1);
	}

	@Test
	void marksTheDecksIAlreadyJoinedWithMyRole() {
		Deck joined = deck(SOMEONE_ELSE, "Joined", null, Visibility.PUBLIC, "2026-01-02T10:00:00Z");
		deck(SOMEONE_ELSE, "Open", null, Visibility.PUBLIC, "2026-01-01T10:00:00Z");
		members.save(DeckMember.joinAsReader(joined.getId(), ME));
		members.save(DeckMember.joinAsReader(joined.getId(), SOMEONE_ELSE));

		assertThat(useCase.execute(ME, "")).extracting(DeckView::role).containsExactly(DeckRole.READER, null);
	}

	@Test
	void returnsAtMostFiftyDecks() {
		IntStream.range(0, 60).forEach(i -> deck(SOMEONE_ELSE, "Deck " + i, null, Visibility.PUBLIC, "2026-01-01T10:00:00Z"));

		assertThat(useCase.execute(ME, "")).hasSize(SearchPublicDecksUseCaseImpl.MAX_RESULTS);
	}

	@Test
	void cutsATermLongerThanTheLimit() {
		deck(SOMEONE_ELSE, "a".repeat(100), null, Visibility.PUBLIC, "2026-01-01T10:00:00Z");

		assertThat(useCase.execute(ME, "a".repeat(100) + "zzz")).hasSize(1);
	}
}
