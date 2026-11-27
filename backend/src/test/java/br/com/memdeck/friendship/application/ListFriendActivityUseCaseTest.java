package br.com.memdeck.friendship.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.review.application.InMemoryReviewLogGateway;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class ListFriendActivityUseCaseTest {

	private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
	private static final LocalDate TODAY = LocalDate.parse("2026-11-27");

	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryFriendshipGateway friendships = new InMemoryFriendshipGateway();
	private final InMemoryReviewLogGateway logs = new InMemoryReviewLogGateway();
	private final ListFriendActivityUseCase useCase = new ListFriendActivityUseCaseImpl(users, friendships, logs,
			Clock.fixed(NOW, ZoneOffset.UTC));

	private final User me = register("Julia");

	private User register(String name) {
		return users.save(User.register(name, name.toLowerCase(), name.toLowerCase() + "@example.com", "hash"));
	}

	private User friend(String name) {
		User other = register(name);
		friendships.save(Friendship.request(me.getId(), other.getId()).accept());
		return other;
	}

	private void reviewed(User user, LocalDate day, int flashcards) {
		for (int i = 0; i < flashcards; i++) {
			logs.recordOncePerDay(user.getId(), UUID.randomUUID(), UUID.randomUUID(), day);
		}
	}

	@Test
	void countsEachFriendOncePerDayNewestFirst() {
		User marcia = friend("Marcia");
		User bruno = friend("Bruno");
		reviewed(marcia, TODAY.minusDays(1), 3);
		reviewed(marcia, TODAY, 2);
		reviewed(bruno, TODAY, 1);

		assertThat(useCase.execute(me.getId()))
				.extracting(FriendActivity::name, FriendActivity::day, FriendActivity::reviewedFlashcards)
				.containsExactly(tuple("Bruno", TODAY, 1L), tuple("Marcia", TODAY, 2L),
						tuple("Marcia", TODAY.minusDays(1), 3L));
	}

	@Test
	void leavesOutMyselfStrangersAndPendingRequests() {
		User stranger = register("Stranger");
		User pending = register("Pending");
		friendships.save(Friendship.request(me.getId(), pending.getId()));
		reviewed(me, TODAY, 1);
		reviewed(stranger, TODAY, 1);
		reviewed(pending, TODAY, 1);

		assertThat(useCase.execute(me.getId())).isEmpty();
	}

	@Test
	void showsOnlyTheLastThirtyDays() {
		User marcia = friend("Marcia");
		reviewed(marcia, TODAY.minusDays(ListFriendActivityUseCaseImpl.DAYS - 1), 1);
		reviewed(marcia, TODAY.minusDays(ListFriendActivityUseCaseImpl.DAYS), 1);

		assertThat(useCase.execute(me.getId())).hasSize(1);
	}

	@Test
	void showsAtMostTenEntries() {
		User marcia = friend("Marcia");
		for (int day = 0; day < ListFriendActivityUseCaseImpl.MAX_ENTRIES + 3; day++) {
			reviewed(marcia, TODAY.minusDays(day), 1);
		}

		assertThat(useCase.execute(me.getId())).hasSize(ListFriendActivityUseCaseImpl.MAX_ENTRIES);
	}
}
