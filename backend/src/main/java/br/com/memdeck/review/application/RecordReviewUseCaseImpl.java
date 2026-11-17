package br.com.memdeck.review.application;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.flashcard.application.FlashcardGateway;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.review.domain.ReviewSchedule;

@Service
@Transactional
public class RecordReviewUseCaseImpl implements RecordReviewUseCase {

	private final DeckGateway decks;
	private final FlashcardGateway flashcards;
	private final ReviewGateway reviews;
	private final Clock clock;

	public RecordReviewUseCaseImpl(DeckGateway decks, FlashcardGateway flashcards, ReviewGateway reviews,
			Clock clock) {
		this.decks = decks;
		this.flashcards = flashcards;
		this.reviews = reviews;
		this.clock = clock;
	}

	@Override
	public ReviewSchedule execute(RecordReviewCommand command) {
		decks.findById(command.deckId())
				.filter(found -> found.canBeViewedBy(command.requesterId()))
				.orElseThrow(() -> new DeckNotFoundException(command.deckId()));
		flashcards.findById(command.flashcardId())
				.filter(found -> found.getDeckId().equals(command.deckId()))
				.orElseThrow(() -> new FlashcardNotFoundException(command.flashcardId()));
		LocalDate today = LocalDate.now(clock);

		ReviewSchedule updated = reviews.findByUserAndFlashcard(command.requesterId(), command.flashcardId())
				.map(existing -> existing.review(command.grade(), today))
				.orElseGet(() -> ReviewSchedule.firstReview(command.requesterId(), command.flashcardId(),
						command.grade(), today));
		return reviews.save(updated);
	}
}
