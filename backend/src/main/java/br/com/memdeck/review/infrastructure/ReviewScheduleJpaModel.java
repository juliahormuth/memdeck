package br.com.memdeck.review.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_schedules")
class ReviewScheduleJpaModel {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "flashcard_id", nullable = false)
	private UUID flashcardId;

	@Column(nullable = false)
	private int repetitions;

	@Column(name = "interval_days", nullable = false)
	private int intervalDays;

	@Column(name = "ease_factor", nullable = false, precision = 4, scale = 2)
	private BigDecimal easeFactor;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "first_reviewed_on", nullable = false)
	private LocalDate firstReviewedOn;

	@Column(name = "last_reviewed_on", nullable = false)
	private LocalDate lastReviewedOn;

	protected ReviewScheduleJpaModel() {
	}

	ReviewScheduleJpaModel(UUID id, UUID userId, UUID flashcardId, int repetitions, int intervalDays,
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

	UUID getId() {
		return id;
	}

	UUID getUserId() {
		return userId;
	}

	UUID getFlashcardId() {
		return flashcardId;
	}

	int getRepetitions() {
		return repetitions;
	}

	int getIntervalDays() {
		return intervalDays;
	}

	BigDecimal getEaseFactor() {
		return easeFactor;
	}

	LocalDate getDueDate() {
		return dueDate;
	}

	LocalDate getFirstReviewedOn() {
		return firstReviewedOn;
	}

	LocalDate getLastReviewedOn() {
		return lastReviewedOn;
	}
}
