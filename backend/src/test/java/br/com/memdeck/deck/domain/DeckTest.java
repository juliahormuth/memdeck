package br.com.memdeck.deck.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DeckTest {

	@Test
	void createsDeckWithTrimmedNameAndGeneratedIdentity() {
		Deck deck = Deck.create("  Anatomy  ", "Bones", Visibility.PRIVATE);

		assertThat(deck.getId()).isNotNull();
		assertThat(deck.getName()).isEqualTo("Anatomy");
		assertThat(deck.getDescription()).isEqualTo("Bones");
		assertThat(deck.getVisibility()).isEqualTo(Visibility.PRIVATE);
		assertThat(deck.getCreatedAt()).isNotNull();
	}

	@Test
	void acceptsMissingDescription() {
		Deck deck = Deck.create("English", null, Visibility.PUBLIC);

		assertThat(deck.getDescription()).isNull();
	}

	@Test
	void rejectsBlankName() {
		assertThatThrownBy(() -> Deck.create("   ", null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsNullName() {
		assertThatThrownBy(() -> Deck.create(null, null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsNameLongerThanLimit() {
		String tooLong = "a".repeat(Deck.NAME_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Deck.create(tooLong, null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void acceptsNameWithExactlyTheLimit() {
		Deck deck = Deck.create("a".repeat(Deck.NAME_MAX_LENGTH), null, Visibility.PUBLIC);

		assertThat(deck.getName()).hasSize(Deck.NAME_MAX_LENGTH);
	}

	@Test
	void rejectsDescriptionLongerThanLimit() {
		String tooLong = "a".repeat(Deck.DESCRIPTION_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Deck.create("English", tooLong, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsMissingVisibility() {
		assertThatThrownBy(() -> Deck.create("English", null, null))
				.isInstanceOf(InvalidDeckException.class);
	}
}
