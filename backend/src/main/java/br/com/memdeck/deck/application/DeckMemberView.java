package br.com.memdeck.deck.application;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;

public record DeckMemberView(UUID userId, String name, DeckRole role, Instant joinedAt) {
}
