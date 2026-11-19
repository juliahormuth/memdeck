package br.com.memdeck.review.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReviewActivityTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

	private static ReviewActivity activity(LocalDate... days) {
		return ReviewActivity.of(List.of(days), TODAY);
	}

	@Test
	void countsDistinctReviewedDays() {
		assertThat(activity(TODAY, TODAY, TODAY.minusDays(3), TODAY.minusDays(5)).reviewedDays()).isEqualTo(3);
		assertThat(activity().reviewedDays()).isZero();
	}

	@Test
	void theStreakCountsConsecutiveDaysUpToToday() {
		assertThat(activity(TODAY, TODAY.minusDays(1), TODAY.minusDays(2)).currentStreak()).isEqualTo(3);
	}

	@Test
	void aStreakThatEndedYesterdayIsStillAliveBecauseTodayIsNotOverYet() {
		assertThat(activity(TODAY.minusDays(1), TODAY.minusDays(2)).currentStreak()).isEqualTo(2);
	}

	@Test
	void missingYesterdayAndTodayBreaksTheStreak() {
		assertThat(activity(TODAY.minusDays(2), TODAY.minusDays(3)).currentStreak()).isZero();
	}

	@Test
	void aGapStopsTheStreakAtTheGap() {
		assertThat(activity(TODAY, TODAY.minusDays(1), TODAY.minusDays(3), TODAY.minusDays(4)).currentStreak())
				.isEqualTo(2);
	}

	@Test
	void noReviewsMeansNoStreak() {
		assertThat(activity().currentStreak()).isZero();
	}
}
