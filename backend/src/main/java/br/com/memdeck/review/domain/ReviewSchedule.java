package br.com.memdeck.review.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class ReviewSchedule {

	public static final BigDecimal INITIAL_EASE_FACTOR = new BigDecimal("2.50");
	public static final BigDecimal MIN_EASE_FACTOR = new BigDecimal("1.30");

	private static final int FIRST_INTERVAL_DAYS = 1;
	private static final int SECOND_INTERVAL_DAYS = 6;

	private final UUID id;
	private final UUID userId;
	private final UUID flashcardId;
	private final int repetitions;
	private final int intervalDays;
	private final BigDecimal easeFactor;
	private final LocalDate dueDate;
	private final LocalDate firstReviewedOn;
	private final LocalDate lastReviewedOn;

	private ReviewSchedule(UUID id, UUID userId, UUID flashcardId, int repetitions, int intervalDays,
			BigDecimal easeFactor, LocalDate dueDate, LocalDate firstReviewedOn, LocalDate lastReviewedOn) {
		this.id = id;
		this.userId = userId;
		this.flashcardId = flashcardId;
		this.repetitions = repetitions;
		this.intervalDays = intervalDays;
		this.easeFactor = easeFactor;
		this.dueDate = dueDate;
		this.firstReviewedOn = firstReviewedOn;
		this.lastReviewedOn = lastReviewedOn;
	}

	public static ReviewSchedule firstReview(UUID userId, UUID flashcardId, ReviewGrade grade, LocalDate today) {
		ReviewSchedule untouched = new ReviewSchedule(UUID.randomUUID(), userId, flashcardId, 0, 0,
				INITIAL_EASE_FACTOR, today, today, today);
		return untouched.review(grade, today);
	}

	public static ReviewSchedule restore(UUID id, UUID userId, UUID flashcardId, int repetitions, int intervalDays,
			BigDecimal easeFactor, LocalDate dueDate, LocalDate firstReviewedOn, LocalDate lastReviewedOn) {
		return new ReviewSchedule(id, userId, flashcardId, repetitions, intervalDays, easeFactor, dueDate,
				firstReviewedOn, lastReviewedOn);
	}

	public ReviewSchedule review(ReviewGrade grade, LocalDate today) {
		if (!grade.isRecalled()) {
			return next(0, FIRST_INTERVAL_DAYS, easeFactor, today);
		}
		return next(repetitions + 1, nextInterval(), nextEaseFactor(grade), today);
	}

	private int nextInterval() {
		if (repetitions == 0) {
			return FIRST_INTERVAL_DAYS;
		}
		if (repetitions == 1) {
			return SECOND_INTERVAL_DAYS;
		}
		return BigDecimal.valueOf(intervalDays).multiply(easeFactor).setScale(0, RoundingMode.HALF_UP).intValue();
	}

	// SM-2: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)), never below 1.3
	private BigDecimal nextEaseFactor(ReviewGrade grade) {
		BigDecimal missing = BigDecimal.valueOf(5 - grade.quality());
		BigDecimal penalty = missing.multiply(new BigDecimal("0.08").add(missing.multiply(new BigDecimal("0.02"))));
		BigDecimal updated = easeFactor.add(new BigDecimal("0.10")).subtract(penalty);
		return updated.max(MIN_EASE_FACTOR);
	}

	private ReviewSchedule next(int newRepetitions, int newIntervalDays, BigDecimal newEaseFactor, LocalDate today) {
		return new ReviewSchedule(id, userId, flashcardId, newRepetitions, newIntervalDays, newEaseFactor,
				today.plusDays(newIntervalDays), firstReviewedOn, today);
	}

	public boolean isDueOn(LocalDate day) {
		return !dueDate.isAfter(day);
	}

	public boolean wasIntroducedOn(LocalDate day) {
		return firstReviewedOn.equals(day);
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getFlashcardId() {
		return flashcardId;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public int getIntervalDays() {
		return intervalDays;
	}

	public BigDecimal getEaseFactor() {
		return easeFactor;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public LocalDate getFirstReviewedOn() {
		return firstReviewedOn;
	}

	public LocalDate getLastReviewedOn() {
		return lastReviewedOn;
	}
}
