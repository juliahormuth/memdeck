package br.com.memdeck.invitation.application;

import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;

public record InviteToDeckCommand(UUID requesterId, UUID deckId, String email, DeckRole role) {
}
