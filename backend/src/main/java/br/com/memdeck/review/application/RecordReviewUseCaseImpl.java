package br.com.memdeck.review.application;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.flashcard.application.FlashcardGateway;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.review.domain.ReviewSchedule;

@Service
@Transactional
public class RecordReviewUseCaseImpl implements RecordReviewUseCase {

	private final DeckAccessFinder access;
	private final FlashcardGateway flashcards;
	private final ReviewGateway reviews;
	private final ReviewLogGateway logs;
	private final Clock clock;

	public RecordReviewUseCaseImpl(DeckAccessFinder access, FlashcardGateway flashcards, ReviewGateway reviews,
			ReviewLogGateway logs, Clock clock) {
		this.access = access;
		this.flashcards = flashcards;
		this.reviews = reviews;
		this.logs = logs;
		this.clock = clock;
	}

	@Override
	public ReviewSchedule execute(RecordReviewCommand command) {
		access.viewable(command.deckId(), command.requesterId());
		flashcards.findById(command.flashcardId())
				.filter(found -> found.getDeckId().equals(command.deckId()))
				.orElseThrow(() -> new FlashcardNotFoundException(command.flashcardId()));
		LocalDate today = LocalDate.now(clock);

		ReviewSchedule updated = reviews.findByUserAndFlashcard(command.requesterId(), command.flashcardId())
				.map(existing -> existing.review(command.grade(), today))
				.orElseGet(() -> ReviewSchedule.firstReview(command.requesterId(), command.flashcardId(),
						command.grade(), today));
		ReviewSchedule saved = reviews.save(updated);
		logs.recordOncePerDay(command.requesterId(), command.deckId(), command.flashcardId(), today);
		return saved;
	}
}
