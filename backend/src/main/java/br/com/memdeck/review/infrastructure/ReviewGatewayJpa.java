package br.com.memdeck.review.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.review.application.ReviewGateway;
import br.com.memdeck.review.domain.ReviewSchedule;

@Component
class ReviewGatewayJpa implements ReviewGateway {

	private final ReviewScheduleJpaRepository repository;

	ReviewGatewayJpa(ReviewScheduleJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public ReviewSchedule save(ReviewSchedule schedule) {
		return ReviewScheduleMapper.toDomain(repository.save(ReviewScheduleMapper.toModel(schedule)));
	}

	@Override
	public Optional<ReviewSchedule> findByUserAndFlashcard(UUID userId, UUID flashcardId) {
		return repository.findByUserIdAndFlashcardId(userId, flashcardId).map(ReviewScheduleMapper::toDomain);
	}

	@Override
	public List<ReviewSchedule> findByUserAndDeck(UUID userId, UUID deckId) {
		return repository.findByUserIdAndDeckId(userId, deckId).stream().map(ReviewScheduleMapper::toDomain).toList();
	}
}
