package br.com.memdeck.deck.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.Visibility;

public record DeckResponse(
		UUID id,
		String name,
		String description,
		Visibility visibility,
		Instant createdAt,
		boolean ownedByMe,
		DeckRole role) {

	static DeckResponse from(DeckView view) {
		Deck deck = view.deck();
		return new DeckResponse(
				deck.getId(),
				deck.getName(),
				deck.getDescription(),
				deck.getVisibility(),
				deck.getCreatedAt(),
				view.role() == DeckRole.OWNER,
				view.role());
	}

	static DeckResponse ownedBy(Deck deck) {
		return from(DeckView.ownedBy(deck));
	}
}
