package br.com.memdeck.deck.domain;

import java.time.Instant;
import java.util.UUID;

public class DeckMember {

	private final UUID id;
	private final UUID deckId;
	private final UUID userId;
	private final DeckRole role;
	private final Instant joinedAt;

	private DeckMember(UUID id, UUID deckId, UUID userId, DeckRole role, Instant joinedAt) {
		this.id = id;
		this.deckId = deckId;
		this.userId = userId;
		this.role = role;
		this.joinedAt = joinedAt;
	}

	public static DeckMember joinAsReader(UUID deckId, UUID userId) {
		return new DeckMember(UUID.randomUUID(), deckId, userId, DeckRole.READER, Instant.now());
	}

	public static DeckMember restore(UUID id, UUID deckId, UUID userId, DeckRole role, Instant joinedAt) {
		return new DeckMember(id, deckId, userId, role, joinedAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getDeckId() {
		return deckId;
	}

	public UUID getUserId() {
		return userId;
	}

	public DeckRole getRole() {
		return role;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
