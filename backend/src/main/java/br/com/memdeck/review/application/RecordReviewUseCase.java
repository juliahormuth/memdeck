package br.com.memdeck.review.application;

import br.com.memdeck.review.domain.ReviewSchedule;

public interface RecordReviewUseCase {

	ReviewSchedule execute(RecordReviewCommand command);
}
