package br.com.memdeck.review.infrastructure;

import br.com.memdeck.review.domain.ReviewSchedule;

final class ReviewScheduleMapper {

	private ReviewScheduleMapper() {
	}

	static ReviewScheduleJpaModel toModel(ReviewSchedule schedule) {
		return new ReviewScheduleJpaModel(
				schedule.getId(),
				schedule.getUserId(),
				schedule.getFlashcardId(),
				schedule.getRepetitions(),
				schedule.getIntervalDays(),
				schedule.getEaseFactor(),
				schedule.getDueDate(),
				schedule.getFirstReviewedOn(),
				schedule.getLastReviewedOn());
	}

	static ReviewSchedule toDomain(ReviewScheduleJpaModel model) {
		return ReviewSchedule.restore(
				model.getId(),
				model.getUserId(),
				model.getFlashcardId(),
				model.getRepetitions(),
				model.getIntervalDays(),
				model.getEaseFactor(),
				model.getDueDate(),
				model.getFirstReviewedOn(),
				model.getLastReviewedOn());
	}
}
