package br.com.memdeck.friendship.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.com.memdeck.friendship.domain.TooManySearchesException;

// kept in memory because the application runs as a single instance; it resets on restart
@Component
public class SearchThrottle {

	public static final int MAX_SEARCHES = 20;
	public static final Duration WINDOW = Duration.ofHours(1);

	private final Map<UUID, Deque<Instant>> searches = new ConcurrentHashMap<>();
	private final Clock clock;

	public SearchThrottle(Clock clock) {
		this.clock = clock;
	}

	public void register(UUID userId) {
		Instant now = clock.instant();
		searches.compute(userId, (id, previous) -> {
			Deque<Instant> recent = previous == null ? new ArrayDeque<>() : previous;
			while (!recent.isEmpty() && !recent.peekFirst().isAfter(now.minus(WINDOW))) {
				recent.removeFirst();
			}
			if (recent.size() >= MAX_SEARCHES) {
				throw new TooManySearchesException(Duration.between(now, recent.peekFirst().plus(WINDOW)));
			}
			recent.addLast(now);
			return recent;
		});
	}
}
