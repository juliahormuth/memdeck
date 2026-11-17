package br.com.memdeck.review.presentation;

import java.util.UUID;

import br.com.memdeck.review.application.RecordReviewCommand;
import br.com.memdeck.review.domain.ReviewGrade;
import jakarta.validation.constraints.NotNull;

public record RecordReviewRequest(@NotNull ReviewGrade grade) {

	RecordReviewCommand toCommand(UUID requesterId, UUID deckId, UUID flashcardId) {
		return new RecordReviewCommand(requesterId, deckId, flashcardId, grade);
	}
}
