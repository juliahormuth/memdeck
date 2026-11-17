package br.com.memdeck.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.application.InMemoryFlashcardGateway;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.review.domain.ReviewGrade;
import br.com.memdeck.review.domain.ReviewSchedule;

class ListStudyQueueUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC);
	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final InMemoryReviewGateway reviews = new InMemoryReviewGateway(flashcards);
	private final ListStudyQueueUseCase useCase = new ListStudyQueueUseCaseImpl(decks, flashcards, reviews, clock);

	private Deck deck(Visibility visibility) {
		return decks.save(Deck.create(OWNER, "Anatomy", null, visibility));
	}

	private Flashcard card(Deck deck, String front) {
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return flashcards.save(Flashcard.create(deck.getId(), front, "answer"));
	}

	private void reviewedOn(UUID userId, Flashcard card, ReviewGrade grade, LocalDate day) {
		reviews.save(ReviewSchedule.firstReview(userId, card.getId(), grade, day));
	}

	private List<String> fronts(List<StudyCard> queue) {
		return queue.stream().map(item -> item.flashcard().getFront()).toList();
	}

	@Test
	void anUntouchedDeckOffersItsCardsAsNewOldestFirst() {
		Deck deck = deck(Visibility.PRIVATE);
		card(deck, "first");
		card(deck, "second");

		List<StudyCard> queue = useCase.execute(deck.getId(), OWNER);

		assertThat(fronts(queue)).containsExactly("first", "second");
		assertThat(queue).allMatch(StudyCard::newCard);
	}

	@Test
	void offersOnlyTheCardsDueTodayAndPutsThemBeforeTheNewOnes() {
		Deck deck = deck(Visibility.PRIVATE);
		Flashcard overdue = card(deck, "overdue");
		Flashcard dueToday = card(deck, "due today");
		Flashcard future = card(deck, "future");
		card(deck, "new");
		reviewedOn(OWNER, overdue, ReviewGrade.GOOD, TODAY.minusDays(10));
		reviewedOn(OWNER, dueToday, ReviewGrade.GOOD, TODAY.minusDays(1));
		reviewedOn(OWNER, future, ReviewGrade.GOOD, TODAY);

		List<StudyCard> queue = useCase.execute(deck.getId(), OWNER);

		assertThat(fronts(queue)).containsExactly("overdue", "due today", "new");
		assertThat(queue.stream().map(StudyCard::newCard)).containsExactly(false, false, true);
	}

	@Test
	void offersAtMostTwentyNewCardsPerDay() {
		Deck deck = deck(Visibility.PRIVATE);
		IntStream.rangeClosed(1, 25).forEach(i -> card(deck, "card " + i));

		List<StudyCard> queue = useCase.execute(deck.getId(), OWNER);

		assertThat(queue).hasSize(20);
		assertThat(queue.get(0).flashcard().getFront()).isEqualTo("card 1");
	}

	@Test
	void cardsIntroducedTodayCountAgainstTheDailyLimit() {
		Deck deck = deck(Visibility.PRIVATE);
		IntStream.rangeClosed(1, 25).forEach(i -> card(deck, "card " + i));
		List<StudyCard> before = useCase.execute(deck.getId(), OWNER);
		before.stream().limit(5).forEach(item -> reviewedOn(OWNER, item.flashcard(), ReviewGrade.AGAIN, TODAY));

		List<StudyCard> queue = useCase.execute(deck.getId(), OWNER);

		assertThat(queue).hasSize(15);
		assertThat(queue).allMatch(StudyCard::newCard);
	}

	@Test
	void cardsIntroducedOnEarlierDaysDoNotCountAgainstTodaysLimit() {
		Deck deck = deck(Visibility.PRIVATE);
		IntStream.rangeClosed(1, 25).forEach(i -> card(deck, "card " + i));
		List<StudyCard> before = useCase.execute(deck.getId(), OWNER);
		before.stream().limit(20).forEach(item -> reviewedOn(OWNER, item.flashcard(), ReviewGrade.GOOD, TODAY.minusDays(5)));

		List<StudyCard> queue = useCase.execute(deck.getId(), OWNER);

		assertThat(queue.stream().filter(StudyCard::newCard)).hasSize(5);
		assertThat(queue.stream().filter(item -> !item.newCard())).hasSize(20);
	}

	@Test
	void theDailyLimitIsPerDeck() {
		Deck first = deck(Visibility.PRIVATE);
		Deck second = deck(Visibility.PRIVATE);
		IntStream.rangeClosed(1, 20).forEach(i -> reviewedOn(OWNER, card(first, "a" + i), ReviewGrade.GOOD, TODAY));
		card(second, "b1");

		assertThat(useCase.execute(first.getId(), OWNER)).isEmpty();
		assertThat(fronts(useCase.execute(second.getId(), OWNER))).containsExactly("b1");
	}

	@Test
	void eachUserHasTheirOwnSchedule() {
		Deck deck = deck(Visibility.PUBLIC);
		Flashcard card = card(deck, "shared");
		reviewedOn(OWNER, card, ReviewGrade.GOOD, TODAY);

		assertThat(useCase.execute(deck.getId(), OWNER)).isEmpty();
		List<StudyCard> forStranger = useCase.execute(deck.getId(), STRANGER);
		assertThat(fronts(forStranger)).containsExactly("shared");
		assertThat(forStranger.get(0).newCard()).isTrue();
	}

	@Test
	void aStrangerCannotStudyAPrivateDeck() {
		Deck deck = deck(Visibility.PRIVATE);
		card(deck, "secret");

		assertThatThrownBy(() -> useCase.execute(deck.getId(), STRANGER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void failsWhenTheDeckDoesNotExist() {
		assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), OWNER)).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void anEmptyDeckHasNothingToStudy() {
		Deck deck = deck(Visibility.PRIVATE);

		assertThat(useCase.execute(deck.getId(), OWNER)).isEmpty();
	}
}
