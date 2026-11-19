package br.com.memdeck.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.InMemoryDeckMemberGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckParticipantException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class GetDeckRankingUseCaseTest {

	private static final LocalDate DAY = LocalDate.of(2026, 11, 19);

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryReviewLogGateway logs = new InMemoryReviewLogGateway();
	private final GetDeckRankingUseCase useCase = new GetDeckRankingUseCaseImpl(
			new DeckAccessFinder(decks, members), members, users, logs);

	private final User owner = users.save(User.register("Olivia", "olivia@example.com", "hash"));
	private final User editor = users.save(User.register("Eduardo", "eduardo@example.com", "hash"));
	private final User reader = users.save(User.register("Rita", "rita@example.com", "hash"));
	private final User stranger = users.save(User.register("Samuel", "samuel@example.com", "hash"));
	private final Deck deck = decks.save(Deck.create(owner.getId(), "Anatomy", null, Visibility.PUBLIC));

	private void review(User user, int times) {
		for (int i = 0; i < times; i++) {
			logs.recordOncePerDay(user.getId(), deck.getId(), UUID.randomUUID(), DAY);
		}
	}

	private void team() {
		members.save(DeckMember.join(deck.getId(), editor.getId(), DeckRole.EDITOR));
		members.save(DeckMember.join(deck.getId(), reader.getId(), DeckRole.READER));
	}

	@Test
	void ranksTheOwnerAndTheMembersByReviewedFlashcardsDescending() {
		team();
		review(owner, 2);
		review(editor, 5);
		review(reader, 3);

		assertThat(useCase.execute(deck.getId(), owner.getId()))
				.extracting(RankingEntry::name, RankingEntry::reviewedFlashcards, RankingEntry::position)
				.containsExactly(org.assertj.core.groups.Tuple.tuple("Eduardo", 5L, 1),
						org.assertj.core.groups.Tuple.tuple("Rita", 3L, 2),
						org.assertj.core.groups.Tuple.tuple("Olivia", 2L, 3));
	}

	@Test
	void participantsWhoNeverReviewedAppearWithZeroAndTiesShareThePosition() {
		team();
		review(editor, 2);
		review(reader, 2);

		assertThat(useCase.execute(deck.getId(), editor.getId()))
				.extracting(RankingEntry::name, RankingEntry::position)
				.containsExactly(org.assertj.core.groups.Tuple.tuple("Eduardo", 1),
						org.assertj.core.groups.Tuple.tuple("Rita", 1),
						org.assertj.core.groups.Tuple.tuple("Olivia", 3));
		assertThat(useCase.execute(deck.getId(), reader.getId()).get(2).reviewedFlashcards()).isZero();
	}

	@Test
	void onlyReviewsOfThisDeckCountAndPeopleOutsideTheDeckDoNotAppear() {
		Deck other = decks.save(Deck.create(owner.getId(), "English", null, Visibility.PUBLIC));
		logs.recordOncePerDay(owner.getId(), other.getId(), UUID.randomUUID(), DAY);
		review(stranger, 4);

		assertThat(useCase.execute(deck.getId(), owner.getId())).extracting(RankingEntry::name).containsExactly("Olivia");
		assertThat(useCase.execute(deck.getId(), owner.getId()).get(0).reviewedFlashcards()).isZero();
	}

	@Test
	void aNonParticipantOfAPublicDeckCannotSeeTheRanking() {
		assertThatThrownBy(() -> useCase.execute(deck.getId(), stranger.getId()))
				.isInstanceOf(NotDeckParticipantException.class);
	}

	@Test
	void aPrivateDeckStaysHiddenAndAMissingOneToo() {
		Deck hidden = decks.save(Deck.create(owner.getId(), "Secret", null, Visibility.PRIVATE));

		assertThatThrownBy(() -> useCase.execute(hidden.getId(), stranger.getId())).isInstanceOf(DeckNotFoundException.class);
		assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), owner.getId())).isInstanceOf(DeckNotFoundException.class);
	}
}
