package br.com.memdeck.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.friendship.application.InMemoryFriendshipGateway;
import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.review.application.InMemoryReviewLogGateway;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.Avatar;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;
import br.com.memdeck.user.domain.UserNotFoundException;

class ProfileUseCasesTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryReviewLogGateway logs = new InMemoryReviewLogGateway();
	private final InMemoryFriendshipGateway friendships = new InMemoryFriendshipGateway();
	private final GetProfileUseCase getProfile = new GetProfileUseCaseImpl(users, logs, friendships, clock);
	private final UpdateProfileUseCase updateProfile = new UpdateProfileUseCaseImpl(users);

	private final User julia = users.save(User.register("Julia", "julia@example.com", "hash"));

	@Test
	void aNewProfileHasTheDefaultAvatarAndNoActivity() {
		ProfileView view = getProfile.execute(julia.getId());

		assertThat(view.user().getName()).isEqualTo("Julia");
		assertThat(view.user().getAvatar()).isEqualTo(Avatar.CAT);
		assertThat(view.reviewedDays()).isZero();
		assertThat(view.streakDays()).isZero();
		assertThat(view.friends()).isZero();
	}

	@Test
	void showsReviewedDaysTheCurrentStreakAndTheNumberOfFriends() {
		UUID deck = UUID.randomUUID();
		for (int daysAgo : new int[] { 0, 1, 2, 5 }) {
			logs.recordOncePerDay(julia.getId(), deck, UUID.randomUUID(), TODAY.minusDays(daysAgo));
		}
		logs.recordOncePerDay(julia.getId(), deck, UUID.randomUUID(), TODAY);
		friendships.save(Friendship.request(julia.getId(), UUID.randomUUID()).accept());
		friendships.save(Friendship.request(UUID.randomUUID(), julia.getId()));

		ProfileView view = getProfile.execute(julia.getId());

		assertThat(view.reviewedDays()).isEqualTo(4);
		assertThat(view.streakDays()).isEqualTo(3);
		assertThat(view.friends()).isEqualTo(1);
	}

	@Test
	void changesTheNameAndTheAvatarKeepingTheEmail() {
		User updated = updateProfile.execute(new UpdateProfileCommand(julia.getId(), "  Julia Hormuth ", Avatar.DOG));

		assertThat(updated.getName()).isEqualTo("Julia Hormuth");
		assertThat(updated.getAvatar()).isEqualTo(Avatar.DOG);
		assertThat(updated.getEmail()).isEqualTo("julia@example.com");
		assertThat(getProfile.execute(julia.getId()).user().getName()).isEqualTo("Julia Hormuth");
	}

	@Test
	void rejectsABlankOrTooLongNameAndAMissingAvatar() {
		assertThatThrownBy(() -> updateProfile.execute(new UpdateProfileCommand(julia.getId(), "  ", Avatar.CAT)))
				.isInstanceOf(InvalidUserException.class);
		assertThatThrownBy(() -> updateProfile.execute(new UpdateProfileCommand(julia.getId(), "a".repeat(101), Avatar.CAT)))
				.isInstanceOf(InvalidUserException.class);
		assertThatThrownBy(() -> updateProfile.execute(new UpdateProfileCommand(julia.getId(), "Julia", null)))
				.isInstanceOf(InvalidUserException.class);
		assertThat(getProfile.execute(julia.getId()).user().getName()).isEqualTo("Julia");
	}

	@Test
	void failsForAnUnknownUser() {
		UUID unknown = UUID.randomUUID();

		assertThatThrownBy(() -> getProfile.execute(unknown)).isInstanceOf(UserNotFoundException.class);
		assertThatThrownBy(() -> updateProfile.execute(new UpdateProfileCommand(unknown, "x", Avatar.CAT)))
				.isInstanceOf(UserNotFoundException.class);
	}
}
