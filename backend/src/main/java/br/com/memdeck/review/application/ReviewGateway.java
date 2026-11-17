package br.com.memdeck.review.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.review.domain.ReviewSchedule;

public interface ReviewGateway {

	ReviewSchedule save(ReviewSchedule schedule);

	Optional<ReviewSchedule> findByUserAndFlashcard(UUID userId, UUID flashcardId);

	List<ReviewSchedule> findByUserAndDeck(UUID userId, UUID deckId);
}
