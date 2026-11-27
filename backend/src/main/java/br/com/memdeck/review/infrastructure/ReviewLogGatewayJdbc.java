package br.com.memdeck.review.infrastructure;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.memdeck.review.application.DailyReviewCount;
import br.com.memdeck.review.application.ReviewLogGateway;

@Component
class ReviewLogGatewayJdbc implements ReviewLogGateway {

	private final JdbcTemplate jdbc;

	ReviewLogGatewayJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	// The unique key (user, flashcard, day) makes repeated reviews of a card on one day count once.
	@Override
	public void recordOncePerDay(UUID userId, UUID deckId, UUID flashcardId, LocalDate day) {
		jdbc.update("""
				INSERT INTO review_logs (id, user_id, deck_id, flashcard_id, reviewed_on)
				VALUES (?, ?, ?, ?, ?)
				ON CONFLICT (user_id, flashcard_id, reviewed_on) DO NOTHING
				""", UUID.randomUUID(), userId, deckId, flashcardId, Date.valueOf(day));
	}

	@Override
	public List<LocalDate> findReviewedDays(UUID userId) {
		return jdbc.query("SELECT DISTINCT reviewed_on FROM review_logs WHERE user_id = ?",
				(rs, row) -> rs.getDate("reviewed_on").toLocalDate(), userId);
	}

	@Override
	public Map<UUID, Long> countReviewedByUser(UUID deckId) {
		Map<UUID, Long> counts = new HashMap<>();
		jdbc.query("SELECT user_id, count(*) AS total FROM review_logs WHERE deck_id = ? GROUP BY user_id",
				rs -> {
					counts.put(rs.getObject("user_id", UUID.class), rs.getLong("total"));
				}, deckId);
		return counts;
	}

	@Override
	public List<DailyReviewCount> findDailyCounts(Collection<UUID> userIds, LocalDate since, int limit) {
		if (userIds.isEmpty()) {
			return List.of();
		}
		String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));
		List<Object> arguments = new ArrayList<>(userIds);
		arguments.add(Date.valueOf(since));
		arguments.add(limit);
		return jdbc.query("""
				SELECT user_id, reviewed_on, count(*) AS total FROM review_logs
				WHERE user_id IN (%s) AND reviewed_on >= ?
				GROUP BY user_id, reviewed_on
				ORDER BY reviewed_on DESC, user_id
				LIMIT ?
				""".formatted(placeholders),
				(rs, row) -> new DailyReviewCount(rs.getObject("user_id", UUID.class),
						rs.getDate("reviewed_on").toLocalDate(), rs.getLong("total")),
				arguments.toArray());
	}
}
