package br.com.memdeck.review.application;

import java.time.LocalDate;
import java.util.UUID;

public record DailyReviewCount(UUID userId, LocalDate day, long reviewedFlashcards) {
}
