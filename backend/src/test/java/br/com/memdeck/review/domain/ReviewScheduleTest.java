package br.com.memdeck.review.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReviewScheduleTest {

	private static final UUID USER = UUID.randomUUID();
	private static final UUID CARD = UUID.randomUUID();
	private static final LocalDate DAY = LocalDate.of(2026, 11, 17);

	private static ReviewSchedule first(ReviewGrade grade) {
		return ReviewSchedule.firstReview(USER, CARD, grade, DAY);
	}

	@Test
	void theFirstGoodReviewComesBackTomorrowWithTheInitialEaseFactor() {
		ReviewSchedule schedule = first(ReviewGrade.GOOD);

		assertThat(schedule.getIntervalDays()).isEqualTo(1);
		assertThat(schedule.getDueDate()).isEqualTo(DAY.plusDays(1));
		assertThat(schedule.getRepetitions()).isEqualTo(1);
		assertThat(schedule.getEaseFactor()).isEqualByComparingTo("2.50");
		assertThat(schedule.getFirstReviewedOn()).isEqualTo(DAY);
		assertThat(schedule.getLastReviewedOn()).isEqualTo(DAY);
	}

	@Test
	void goodAnswersGrowTheIntervalOneSixFifteenThirtyEightNinetyFive() {
		ReviewSchedule schedule = first(ReviewGrade.GOOD);
		int[] expected = { 1, 6, 15, 38, 95 };
		LocalDate day = DAY;

		for (int i = 0; i < expected.length; i++) {
			assertThat(schedule.getIntervalDays()).isEqualTo(expected[i]);
			assertThat(schedule.getDueDate()).isEqualTo(day.plusDays(expected[i]));
			day = schedule.getDueDate();
			schedule = schedule.review(ReviewGrade.GOOD, day);
		}
	}

	@Test
	void easeFactorMovesByTheSm2FormulaForEachGrade() {
		ReviewSchedule base = first(ReviewGrade.GOOD);

		assertThat(base.review(ReviewGrade.EASY, DAY.plusDays(1)).getEaseFactor()).isEqualByComparingTo("2.60");
		assertThat(base.review(ReviewGrade.GOOD, DAY.plusDays(1)).getEaseFactor()).isEqualByComparingTo("2.50");
		assertThat(base.review(ReviewGrade.HARD, DAY.plusDays(1)).getEaseFactor()).isEqualByComparingTo("2.36");
	}

	@Test
	void hardStillCountsAsRecalledAndKeepsClimbingTheLadder() {
		ReviewSchedule schedule = first(ReviewGrade.GOOD).review(ReviewGrade.HARD, DAY.plusDays(1));

		assertThat(schedule.getIntervalDays()).isEqualTo(6);
		assertThat(schedule.getRepetitions()).isEqualTo(2);
	}

	@Test
	void againSendsTheCardBackToTomorrowAndKeepsTheEaseFactor() {
		ReviewSchedule learned = first(ReviewGrade.EASY).review(ReviewGrade.EASY, DAY.plusDays(1));
		LocalDate day = DAY.plusDays(7);

		ReviewSchedule forgotten = learned.review(ReviewGrade.AGAIN, day);

		assertThat(forgotten.getRepetitions()).isZero();
		assertThat(forgotten.getIntervalDays()).isEqualTo(1);
		assertThat(forgotten.getDueDate()).isEqualTo(day.plusDays(1));
		assertThat(forgotten.getEaseFactor()).isEqualByComparingTo(learned.getEaseFactor());
		assertThat(forgotten.getFirstReviewedOn()).isEqualTo(DAY);
	}

	@Test
	void afterForgettingTheLadderStartsOverAtOneThenSix() {
		ReviewSchedule schedule = first(ReviewGrade.GOOD).review(ReviewGrade.GOOD, DAY.plusDays(1))
				.review(ReviewGrade.AGAIN, DAY.plusDays(7))
				.review(ReviewGrade.GOOD, DAY.plusDays(8));

		assertThat(schedule.getIntervalDays()).isEqualTo(1);
		assertThat(schedule.review(ReviewGrade.GOOD, DAY.plusDays(9)).getIntervalDays()).isEqualTo(6);
	}

	@Test
	void easeFactorNeverDropsBelowOnePointThree() {
		ReviewSchedule schedule = first(ReviewGrade.HARD);
		LocalDate day = DAY;
		for (int i = 0; i < 20; i++) {
			day = schedule.getDueDate();
			schedule = schedule.review(ReviewGrade.HARD, day);
		}

		assertThat(schedule.getEaseFactor()).isEqualByComparingTo(ReviewSchedule.MIN_EASE_FACTOR);
	}

	@Test
	void isDueFromTheDueDateOnAndWasIntroducedOnlyOnTheFirstDay() {
		ReviewSchedule schedule = first(ReviewGrade.GOOD);

		assertThat(schedule.isDueOn(DAY)).isFalse();
		assertThat(schedule.isDueOn(DAY.plusDays(1))).isTrue();
		assertThat(schedule.isDueOn(DAY.plusDays(5))).isTrue();
		assertThat(schedule.wasIntroducedOn(DAY)).isTrue();
		assertThat(schedule.wasIntroducedOn(DAY.plusDays(1))).isFalse();
	}

	@Test
	void gradesMapToTheSm2Qualities() {
		assertThat(ReviewGrade.AGAIN.quality()).isEqualTo(1);
		assertThat(ReviewGrade.HARD.quality()).isEqualTo(3);
		assertThat(ReviewGrade.GOOD.quality()).isEqualTo(4);
		assertThat(ReviewGrade.EASY.quality()).isEqualTo(5);
		assertThat(new BigDecimal("2.50")).isEqualByComparingTo(ReviewSchedule.INITIAL_EASE_FACTOR);
	}
}
