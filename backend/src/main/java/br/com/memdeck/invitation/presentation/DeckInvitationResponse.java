package br.com.memdeck.invitation.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.invitation.application.DeckInvitationView;

public record DeckInvitationResponse(UUID id, String inviteeName, DeckRole role, Instant createdAt) {

	static DeckInvitationResponse from(DeckInvitationView view) {
		return new DeckInvitationResponse(view.id(), view.inviteeName(), view.role(), view.createdAt());
	}
}
