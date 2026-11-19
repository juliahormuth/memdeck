package br.com.memdeck.deck.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.application.DeckMemberView;
import br.com.memdeck.deck.domain.DeckRole;

public record DeckMemberResponse(UUID userId, String name, DeckRole role, Instant joinedAt) {

	static DeckMemberResponse from(DeckMemberView view) {
		return new DeckMemberResponse(view.userId(), view.name(), view.role(), view.joinedAt());
	}
}
