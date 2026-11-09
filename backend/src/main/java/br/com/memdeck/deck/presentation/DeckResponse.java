package br.com.memdeck.deck.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.Visibility;

public record DeckResponse(
		UUID id,
		String name,
		String description,
		Visibility visibility,
		Instant createdAt) {

	static DeckResponse from(Deck deck) {
		return new DeckResponse(
				deck.getId(),
				deck.getName(),
				deck.getDescription(),
				deck.getVisibility(),
				deck.getCreatedAt());
	}
}
