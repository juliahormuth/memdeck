package br.com.memdeck.invitation.application;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;

public record DeckInvitationView(UUID id, String inviteeName, DeckRole role, Instant createdAt) {
}
