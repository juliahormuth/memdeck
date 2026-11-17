package br.com.memdeck.review.presentation;

import java.time.LocalDate;

import br.com.memdeck.review.domain.ReviewSchedule;

public record ReviewResponse(LocalDate dueDate, int intervalDays) {

	static ReviewResponse from(ReviewSchedule schedule) {
		return new ReviewResponse(schedule.getDueDate(), schedule.getIntervalDays());
	}
}
