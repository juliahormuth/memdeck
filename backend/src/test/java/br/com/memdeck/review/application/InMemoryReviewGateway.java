package br.com.memdeck.review.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.flashcard.application.FlashcardGateway;
import br.com.memdeck.review.domain.ReviewSchedule;

class InMemoryReviewGateway implements ReviewGateway {

	private final FlashcardGateway flashcards;
	private final List<ReviewSchedule> store = new ArrayList<>();

	InMemoryReviewGateway(FlashcardGateway flashcards) {
		this.flashcards = flashcards;
	}

	@Override
	public ReviewSchedule save(ReviewSchedule schedule) {
		store.removeIf(existing -> existing.getId().equals(schedule.getId()));
		store.add(schedule);
		return schedule;
	}

	@Override
	public Optional<ReviewSchedule> findByUserAndFlashcard(UUID userId, UUID flashcardId) {
		return store.stream()
				.filter(schedule -> schedule.getUserId().equals(userId) && schedule.getFlashcardId().equals(flashcardId))
				.findFirst();
	}

	@Override
	public List<ReviewSchedule> findByUserAndDeck(UUID userId, UUID deckId) {
		return store.stream()
				.filter(schedule -> schedule.getUserId().equals(userId))
				.filter(schedule -> flashcards.findById(schedule.getFlashcardId())
						.map(card -> card.getDeckId().equals(deckId)).orElse(false))
				.toList();
	}

	int size() {
		return store.size();
	}
}
