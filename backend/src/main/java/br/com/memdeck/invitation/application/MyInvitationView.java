package br.com.memdeck.invitation.application;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;

public record MyInvitationView(UUID id, UUID deckId, String deckName, String inviterName, DeckRole role,
		Instant createdAt) {
}
