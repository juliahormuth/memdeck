package br.com.memdeck.invitation.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.invitation.application.MyInvitationView;

public record MyInvitationResponse(UUID id, UUID deckId, String deckName, String inviterName, DeckRole role,
		Instant createdAt) {

	static MyInvitationResponse from(MyInvitationView view) {
		return new MyInvitationResponse(view.id(), view.deckId(), view.deckName(), view.inviterName(), view.role(),
				view.createdAt());
	}
}
