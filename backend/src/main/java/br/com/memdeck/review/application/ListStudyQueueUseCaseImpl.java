package br.com.memdeck.review.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.flashcard.application.FlashcardGateway;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.review.domain.ReviewSchedule;

@Service
@Transactional(readOnly = true)
public class ListStudyQueueUseCaseImpl implements ListStudyQueueUseCase {

	public static final int NEW_CARDS_PER_DAY = 20;

	private final DeckGateway decks;
	private final FlashcardGateway flashcards;
	private final ReviewGateway reviews;
	private final Clock clock;

	public ListStudyQueueUseCaseImpl(DeckGateway decks, FlashcardGateway flashcards, ReviewGateway reviews,
			Clock clock) {
		this.decks = decks;
		this.flashcards = flashcards;
		this.reviews = reviews;
		this.clock = clock;
	}

	@Override
	public List<StudyCard> execute(UUID deckId, UUID requesterId) {
		decks.findById(deckId)
				.filter(found -> found.canBeViewedBy(requesterId))
				.orElseThrow(() -> new DeckNotFoundException(deckId));
		LocalDate today = LocalDate.now(clock);

		Map<UUID, Flashcard> cardsById = flashcards.findByDeckNewestFirst(deckId).stream()
				.collect(Collectors.toMap(Flashcard::getId, Function.identity()));
		List<ReviewSchedule> schedules = reviews.findByUserAndDeck(requesterId, deckId);

		List<StudyCard> queue = new ArrayList<>(schedules.stream()
				.filter(schedule -> schedule.isDueOn(today))
				.sorted(Comparator.comparing(ReviewSchedule::getDueDate))
				.map(schedule -> new StudyCard(cardsById.get(schedule.getFlashcardId()), false))
				.toList());

		long introducedToday = schedules.stream().filter(schedule -> schedule.wasIntroducedOn(today)).count();
		int newCardsLeft = (int) Math.max(0, NEW_CARDS_PER_DAY - introducedToday);
		Map<UUID, ReviewSchedule> scheduled = schedules.stream()
				.collect(Collectors.toMap(ReviewSchedule::getFlashcardId, Function.identity()));
		cardsById.values().stream()
				.filter(card -> !scheduled.containsKey(card.getId()))
				.sorted(Comparator.comparing(Flashcard::getCreatedAt))
				.limit(newCardsLeft)
				.forEach(card -> queue.add(new StudyCard(card, true)));
		return queue;
	}
}
