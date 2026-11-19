package br.com.memdeck.invitation.presentation;

import java.util.UUID;

import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.domain.DeckRole;

public record AcceptedInvitationResponse(UUID deckId, DeckRole role) {

	static AcceptedInvitationResponse from(DeckView view) {
		return new AcceptedInvitationResponse(view.deck().getId(), view.role());
	}
}
