package br.com.memdeck.profile.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.friendship.application.FriendshipGateway;
import br.com.memdeck.review.application.ReviewLogGateway;
import br.com.memdeck.review.domain.ReviewActivity;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;
import br.com.memdeck.user.domain.UserNotFoundException;

@Service
@Transactional(readOnly = true)
public class GetProfileUseCaseImpl implements GetProfileUseCase {

	private final UserGateway users;
	private final ReviewLogGateway logs;
	private final FriendshipGateway friendships;
	private final Clock clock;

	public GetProfileUseCaseImpl(UserGateway users, ReviewLogGateway logs, FriendshipGateway friendships, Clock clock) {
		this.users = users;
		this.logs = logs;
		this.friendships = friendships;
		this.clock = clock;
	}

	@Override
	public ProfileView execute(UUID userId) {
		User user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		ReviewActivity activity = ReviewActivity.of(logs.findReviewedDays(userId), LocalDate.now(clock));
		return new ProfileView(user, activity.reviewedDays(), activity.currentStreak(),
				friendships.findAcceptedInvolving(userId).size());
	}
}
