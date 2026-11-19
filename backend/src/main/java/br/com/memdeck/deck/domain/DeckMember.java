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
		return join(deckId, userId, DeckRole.READER);
	}

	public static DeckMember join(UUID deckId, UUID userId, DeckRole role) {
		return new DeckMember(UUID.randomUUID(), deckId, userId, validRole(role), Instant.now());
	}

	public DeckMember withRole(DeckRole newRole) {
		return new DeckMember(id, deckId, userId, validRole(newRole), joinedAt);
	}

	private static DeckRole validRole(DeckRole role) {
		if (role != DeckRole.EDITOR && role != DeckRole.READER) {
			throw new InvalidDeckException("A member must be an editor or a reader");
		}
		return role;
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
