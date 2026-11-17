package br.com.memdeck.flashcard.infrastructure;

import br.com.memdeck.flashcard.domain.Flashcard;

final class FlashcardMapper {

	private FlashcardMapper() {
	}

	static FlashcardJpaModel toModel(Flashcard flashcard) {
		return new FlashcardJpaModel(
				flashcard.getId(),
				flashcard.getDeckId(),
				flashcard.getFront(),
				flashcard.getBack(),
				flashcard.getCreatedAt());
	}

	static Flashcard toDomain(FlashcardJpaModel model) {
		return Flashcard.restore(
				model.getId(),
				model.getDeckId(),
				model.getFront(),
				model.getBack(),
				model.getCreatedAt());
	}
}
