package br.com.memdeck.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.InMemoryDeckMemberGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.flashcard.application.InMemoryFlashcardGateway;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.review.domain.ReviewGrade;
import br.com.memdeck.review.domain.ReviewSchedule;

class RecordReviewUseCaseTest {

	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID STRANGER = UUID.randomUUID();
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T23:59:00Z"), ZoneOffset.UTC);
	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryFlashcardGateway flashcards = new InMemoryFlashcardGateway();
	private final InMemoryReviewGateway reviews = new InMemoryReviewGateway(flashcards);
	private final RecordReviewUseCase useCase = new RecordReviewUseCaseImpl(new DeckAccessFinder(decks, new InMemoryDeckMemberGateway()), flashcards, reviews, clock);

	private Deck deck(Visibility visibility) {
		return decks.save(Deck.create(OWNER, "Anatomy", null, visibility));
	}

	private Flashcard card(Deck deck) {
		return flashcards.save(Flashcard.create(deck.getId(), "Femur", "Thigh bone"));
	}

	@Test
	void theFirstReviewCreatesTheScheduleForThatUser() {
		Deck deck = deck(Visibility.PRIVATE);
		Flashcard card = card(deck);

		ReviewSchedule schedule = useCase.execute(
				new RecordReviewCommand(OWNER, deck.getId(), card.getId(), ReviewGrade.GOOD));

		assertThat(schedule.getUserId()).isEqualTo(OWNER);
		assertThat(schedule.getFlashcardId()).isEqualTo(card.getId());
		assertThat(schedule.getDueDate()).isEqualTo(TODAY.plusDays(1));
		assertThat(reviews.size()).isEqualTo(1);
	}

	@Test
	void aSecondReviewUpdatesTheSameSchedule() {
		Deck deck = deck(Visibility.PRIVATE);
		Flashcard card = card(deck);
		RecordReviewCommand good = new RecordReviewCommand(OWNER, deck.getId(), card.getId(), ReviewGrade.GOOD);

		ReviewSchedule first = useCase.execute(good);
		ReviewSchedule second = useCase.execute(good);

		assertThat(second.getId()).isEqualTo(first.getId());
		assertThat(second.getIntervalDays()).isEqualTo(6);
		assertThat(second.getRepetitions()).isEqualTo(2);
		assertThat(reviews.size()).isEqualTo(1);
	}

	@Test
	void aStrangerStudiesAPublicDeckWithTheirOwnSchedule() {
		Deck deck = deck(Visibility.PUBLIC);
		Flashcard card = card(deck);
		useCase.execute(new RecordReviewCommand(OWNER, deck.getId(), card.getId(), ReviewGrade.EASY));

		ReviewSchedule schedule = useCase.execute(
				new RecordReviewCommand(STRANGER, deck.getId(), card.getId(), ReviewGrade.AGAIN));

		assertThat(schedule.getUserId()).isEqualTo(STRANGER);
		assertThat(reviews.size()).isEqualTo(2);
	}

	@Test
	void aStrangerCannotReviewAPrivateDeck() {
		Deck deck = deck(Visibility.PRIVATE);
		Flashcard card = card(deck);

		assertThatThrownBy(() -> useCase.execute(
				new RecordReviewCommand(STRANGER, deck.getId(), card.getId(), ReviewGrade.GOOD)))
				.isInstanceOf(DeckNotFoundException.class);
		assertThat(reviews.size()).isZero();
	}

	@Test
	void failsWhenTheFlashcardBelongsToAnotherDeck() {
		Deck deck = deck(Visibility.PRIVATE);
		Deck other = deck(Visibility.PRIVATE);
		Flashcard card = card(other);

		assertThatThrownBy(() -> useCase.execute(
				new RecordReviewCommand(OWNER, deck.getId(), card.getId(), ReviewGrade.GOOD)))
				.isInstanceOf(FlashcardNotFoundException.class);
	}

	@Test
	void failsWhenTheFlashcardDoesNotExist() {
		Deck deck = deck(Visibility.PRIVATE);

		assertThatThrownBy(() -> useCase.execute(
				new RecordReviewCommand(OWNER, deck.getId(), UUID.randomUUID(), ReviewGrade.GOOD)))
				.isInstanceOf(FlashcardNotFoundException.class);
	}
}
