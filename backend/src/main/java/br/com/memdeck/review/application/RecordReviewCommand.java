package br.com.memdeck.review.application;

import java.util.UUID;

import br.com.memdeck.review.domain.ReviewGrade;

public record RecordReviewCommand(UUID requesterId, UUID deckId, UUID flashcardId, ReviewGrade grade) {
}
