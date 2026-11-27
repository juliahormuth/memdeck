package br.com.memdeck.review.application;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReviewLogGateway {

	void recordOncePerDay(UUID userId, UUID deckId, UUID flashcardId, LocalDate day);

	List<LocalDate> findReviewedDays(UUID userId);

	Map<UUID, Long> countReviewedByUser(UUID deckId);

	List<DailyReviewCount> findDailyCounts(Collection<UUID> userIds, LocalDate since, int limit);
}
