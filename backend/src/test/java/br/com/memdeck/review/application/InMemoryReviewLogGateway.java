package br.com.memdeck.review.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryReviewLogGateway implements ReviewLogGateway {

	private record Entry(UUID userId, UUID deckId, UUID flashcardId, LocalDate day) {
	}

	private final List<Entry> store = new ArrayList<>();

	@Override
	public void recordOncePerDay(UUID userId, UUID deckId, UUID flashcardId, LocalDate day) {
		Entry entry = new Entry(userId, deckId, flashcardId, day);
		if (!store.contains(entry)) {
			store.add(entry);
		}
	}

	@Override
	public List<LocalDate> findReviewedDays(UUID userId) {
		return store.stream().filter(entry -> entry.userId().equals(userId)).map(Entry::day).distinct().toList();
	}

	@Override
	public Map<UUID, Long> countReviewedByUser(UUID deckId) {
		Map<UUID, Long> counts = new HashMap<>();
		store.stream().filter(entry -> entry.deckId().equals(deckId))
				.forEach(entry -> counts.merge(entry.userId(), 1L, Long::sum));
		return counts;
	}

	@Override
	public List<DailyReviewCount> findDailyCounts(Collection<UUID> userIds, LocalDate since, int limit) {
		return store.stream()
				.filter(entry -> userIds.contains(entry.userId()) && !entry.day().isBefore(since))
				.collect(Collectors.groupingBy(entry -> new DailyReviewCount(entry.userId(), entry.day(), 0),
						Collectors.counting()))
				.entrySet().stream()
				.map(group -> new DailyReviewCount(group.getKey().userId(), group.getKey().day(), group.getValue()))
				.sorted(Comparator.comparing(DailyReviewCount::day).reversed().thenComparing(DailyReviewCount::userId))
				.limit(limit)
				.toList();
	}

	public int size() {
		return store.size();
	}
}
