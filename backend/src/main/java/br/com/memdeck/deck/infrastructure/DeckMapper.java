package br.com.memdeck.deck.infrastructure;

import br.com.memdeck.deck.domain.Deck;

final class DeckMapper {

	private DeckMapper() {
	}

	static DeckJpaModel toModel(Deck deck) {
		return new DeckJpaModel(
				deck.getId(),
				deck.getName(),
				deck.getDescription(),
				deck.getVisibility(),
				deck.getCreatedAt());
	}

	static Deck toDomain(DeckJpaModel model) {
		return Deck.restore(
				model.getId(),
				model.getName(),
				model.getDescription(),
				model.getVisibility(),
				model.getCreatedAt());
	}
}
