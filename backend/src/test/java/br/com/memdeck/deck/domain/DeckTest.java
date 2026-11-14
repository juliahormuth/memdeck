package br.com.memdeck.deck.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class DeckTest {

	private static final UUID OWNER = UUID.randomUUID();

	@Test
	void createsDeckWithTrimmedNameAndGeneratedIdentity() {
		Deck deck = Deck.create(OWNER, "  Anatomy  ", "Bones", Visibility.PRIVATE);

		assertThat(deck.getId()).isNotNull();
		assertThat(deck.getName()).isEqualTo("Anatomy");
		assertThat(deck.getDescription()).isEqualTo("Bones");
		assertThat(deck.getVisibility()).isEqualTo(Visibility.PRIVATE);
		assertThat(deck.getCreatedAt()).isNotNull();
	}

	@Test
	void acceptsMissingDescription() {
		Deck deck = Deck.create(OWNER, "English", null, Visibility.PUBLIC);

		assertThat(deck.getDescription()).isNull();
	}

	@Test
	void rejectsBlankName() {
		assertThatThrownBy(() -> Deck.create(OWNER, "   ", null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsNullName() {
		assertThatThrownBy(() -> Deck.create(OWNER, null, null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsNameLongerThanLimit() {
		String tooLong = "a".repeat(Deck.NAME_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Deck.create(OWNER, tooLong, null, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void acceptsNameWithExactlyTheLimit() {
		Deck deck = Deck.create(OWNER, "a".repeat(Deck.NAME_MAX_LENGTH), null, Visibility.PUBLIC);

		assertThat(deck.getName()).hasSize(Deck.NAME_MAX_LENGTH);
	}

	@Test
	void rejectsDescriptionLongerThanLimit() {
		String tooLong = "a".repeat(Deck.DESCRIPTION_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Deck.create(OWNER, "English", tooLong, Visibility.PUBLIC))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void rejectsMissingVisibility() {
		assertThatThrownBy(() -> Deck.create(OWNER, "English", null, null))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void keepsTheOwnerWhoCreatedIt() {
		Deck deck = Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE);

		assertThat(deck.getOwnerId()).isEqualTo(OWNER);
		assertThat(deck.isOwnedBy(OWNER)).isTrue();
		assertThat(deck.isOwnedBy(UUID.randomUUID())).isFalse();
	}

	@Test
	void rejectsMissingOwner() {
		assertThatThrownBy(() -> Deck.create(null, "Anatomy", null, Visibility.PRIVATE))
				.isInstanceOf(InvalidDeckException.class);
	}

	@Test
	void aPrivateDeckCanOnlyBeViewedByItsOwner() {
		Deck deck = Deck.create(OWNER, "Anatomy", null, Visibility.PRIVATE);

		assertThat(deck.canBeViewedBy(OWNER)).isTrue();
		assertThat(deck.canBeViewedBy(UUID.randomUUID())).isFalse();
	}

	@Test
	void aPublicDeckCanBeViewedByAnyone() {
		Deck deck = Deck.create(OWNER, "English", null, Visibility.PUBLIC);

		assertThat(deck.canBeViewedBy(OWNER)).isTrue();
		assertThat(deck.canBeViewedBy(UUID.randomUUID())).isTrue();
	}
}
