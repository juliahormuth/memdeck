package br.com.memdeck.friendship.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.TooManySearchesException;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class SearchUserUseCaseTest {

	private Instant now = Instant.parse("2026-11-27T12:00:00Z");

	private final Clock clock = new Clock() {
		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	};

	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryFriendshipGateway friendships = new InMemoryFriendshipGateway();
	private final SearchUserUseCase search = new SearchUserUseCaseImpl(users, friendships, new SearchThrottle(clock));

	private final User ana = users.save(User.register("Ana", "ana", "ana@example.com", "hash"));
	private final User bruno = users.save(User.register("Bruno", "bruno", "bruno@example.com", "hash"));

	private UserRelation relationSeenBy(UUID requesterId, String username) {
		return search.execute(requesterId, username).orElseThrow().relation();
	}

	@Test
	void findsOnlyTheExactUsername() {
		assertThat(search.execute(ana.getId(), "@Bruno")).map(FoundUser::name).contains("Bruno");
		assertThat(search.execute(ana.getId(), "brun")).isEmpty();
		assertThat(search.execute(ana.getId(), "bruno@example.com")).isEmpty();
	}

	@Test
	void tellsHowTheTwoPeopleAreRelated() {
		assertThat(relationSeenBy(ana.getId(), "ana")).isEqualTo(UserRelation.SELF);
		assertThat(relationSeenBy(ana.getId(), "bruno")).isEqualTo(UserRelation.NONE);

		Friendship request = friendships.save(Friendship.request(ana.getId(), bruno.getId()));
		assertThat(relationSeenBy(ana.getId(), "bruno")).isEqualTo(UserRelation.REQUEST_SENT);
		assertThat(relationSeenBy(bruno.getId(), "ana")).isEqualTo(UserRelation.REQUEST_RECEIVED);

		friendships.save(request.accept());
		assertThat(relationSeenBy(ana.getId(), "bruno")).isEqualTo(UserRelation.FRIEND);
	}

	@Test
	void limitsTheSearchesOfEachPersonPerHour() {
		for (int i = 0; i < SearchThrottle.MAX_SEARCHES; i++) {
			search.execute(ana.getId(), "nobody");
		}

		assertThatThrownBy(() -> search.execute(ana.getId(), "bruno")).isInstanceOf(TooManySearchesException.class)
				.satisfies(error -> assertThat(((TooManySearchesException) error).getRetryAfter())
						.isEqualTo(SearchThrottle.WINDOW));
		assertThat(search.execute(bruno.getId(), "ana")).isPresent();

		now = now.plus(SearchThrottle.WINDOW);
		assertThat(search.execute(ana.getId(), "bruno")).isPresent();
	}
}
