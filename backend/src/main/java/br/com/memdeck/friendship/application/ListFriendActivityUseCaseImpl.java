package br.com.memdeck.friendship.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.review.application.DailyReviewCount;
import br.com.memdeck.review.application.ReviewLogGateway;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ListFriendActivityUseCaseImpl implements ListFriendActivityUseCase {

	static final int DAYS = 30;
	static final int MAX_ENTRIES = 10;

	private final UserGateway users;
	private final FriendshipGateway friendships;
	private final ReviewLogGateway logs;
	private final Clock clock;

	public ListFriendActivityUseCaseImpl(UserGateway users, FriendshipGateway friendships, ReviewLogGateway logs,
			Clock clock) {
		this.users = users;
		this.friendships = friendships;
		this.logs = logs;
		this.clock = clock;
	}

	@Override
	public List<FriendActivity> execute(UUID userId) {
		List<UUID> friendIds = friendships.findAcceptedInvolving(userId).stream()
				.map(friendship -> friendship.otherUser(userId))
				.toList();
		LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
		List<DailyReviewCount> counts = logs.findDailyCounts(friendIds, today.minusDays(DAYS - 1), MAX_ENTRIES);
		Map<UUID, User> friends = users.findAllById(counts.stream().map(DailyReviewCount::userId).distinct().toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return counts.stream()
				.map(count -> {
					User friend = friends.get(count.userId());
					return new FriendActivity(friend.getId(), friend.getName(), friend.getAvatar(), count.day(),
							count.reviewedFlashcards());
				})
				.sorted(Comparator.comparing(FriendActivity::day).reversed()
						.thenComparing(FriendActivity::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}
}
