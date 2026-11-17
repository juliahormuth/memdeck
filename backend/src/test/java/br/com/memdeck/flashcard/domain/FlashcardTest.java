package br.com.memdeck.flashcard.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class FlashcardTest {

	private static final UUID DECK = UUID.randomUUID();

	@Test
	void createsFlashcardWithTrimmedTextAndGeneratedIdentity() {
		Flashcard flashcard = Flashcard.create(DECK, "  Capital of Brazil?  ", "  Brasília ");

		assertThat(flashcard.getId()).isNotNull();
		assertThat(flashcard.getDeckId()).isEqualTo(DECK);
		assertThat(flashcard.getFront()).isEqualTo("Capital of Brazil?");
		assertThat(flashcard.getBack()).isEqualTo("Brasília");
		assertThat(flashcard.getCreatedAt()).isNotNull();
	}

	@Test
	void rejectsMissingDeck() {
		assertThatThrownBy(() -> Flashcard.create(null, "front", "back")).isInstanceOf(InvalidFlashcardException.class);
	}

	@Test
	void rejectsBlankOrMissingFront() {
		assertThatThrownBy(() -> Flashcard.create(DECK, "  ", "back")).isInstanceOf(InvalidFlashcardException.class);
		assertThatThrownBy(() -> Flashcard.create(DECK, null, "back")).isInstanceOf(InvalidFlashcardException.class);
	}

	@Test
	void rejectsBlankOrMissingBack() {
		assertThatThrownBy(() -> Flashcard.create(DECK, "front", "")).isInstanceOf(InvalidFlashcardException.class);
		assertThatThrownBy(() -> Flashcard.create(DECK, "front", null)).isInstanceOf(InvalidFlashcardException.class);
	}

	@Test
	void acceptsTextWithExactlyTheLimits() {
		Flashcard flashcard = Flashcard.create(DECK, "a".repeat(Flashcard.FRONT_MAX_LENGTH),
				"b".repeat(Flashcard.BACK_MAX_LENGTH));

		assertThat(flashcard.getFront()).hasSize(Flashcard.FRONT_MAX_LENGTH);
		assertThat(flashcard.getBack()).hasSize(Flashcard.BACK_MAX_LENGTH);
	}

	@Test
	void rejectsFrontLongerThanLimit() {
		String tooLong = "a".repeat(Flashcard.FRONT_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Flashcard.create(DECK, tooLong, "back")).isInstanceOf(InvalidFlashcardException.class);
	}

	@Test
	void rejectsBackLongerThanLimit() {
		String tooLong = "a".repeat(Flashcard.BACK_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Flashcard.create(DECK, "front", tooLong)).isInstanceOf(InvalidFlashcardException.class);
	}

	@Test
	void changesTheTextKeepingIdentityDeckAndDate() {
		Flashcard flashcard = Flashcard.create(DECK, "Femur", "Bone");

		Flashcard changed = flashcard.withChanges("  Tibia ", " Shin bone ");

		assertThat(changed.getId()).isEqualTo(flashcard.getId());
		assertThat(changed.getDeckId()).isEqualTo(DECK);
		assertThat(changed.getCreatedAt()).isEqualTo(flashcard.getCreatedAt());
		assertThat(changed.getFront()).isEqualTo("Tibia");
		assertThat(changed.getBack()).isEqualTo("Shin bone");
	}

	@Test
	void changesAreValidatedLikeCreation() {
		Flashcard flashcard = Flashcard.create(DECK, "Femur", "Bone");

		assertThatThrownBy(() -> flashcard.withChanges(" ", "x")).isInstanceOf(InvalidFlashcardException.class);
		assertThatThrownBy(() -> flashcard.withChanges("x", "a".repeat(Flashcard.BACK_MAX_LENGTH + 1)))
				.isInstanceOf(InvalidFlashcardException.class);
	}
}
