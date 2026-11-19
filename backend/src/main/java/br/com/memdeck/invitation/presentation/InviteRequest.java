package br.com.memdeck.invitation.presentation;

import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.invitation.application.InviteToDeckCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteRequest(@NotBlank @Size(max = 254) String email, @NotNull DeckRole role) {

	InviteToDeckCommand toCommand(UUID requesterId, UUID deckId) {
		return new InviteToDeckCommand(requesterId, deckId, email, role);
	}
}
