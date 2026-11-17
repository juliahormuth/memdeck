package br.com.memdeck.deck.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deck_members")
class DeckMemberJpaModel {

	@Id
	private UUID id;

	@Column(name = "deck_id", nullable = false)
	private UUID deckId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private DeckRole role;

	@Column(name = "joined_at", nullable = false)
	private Instant joinedAt;

	protected DeckMemberJpaModel() {
	}

	DeckMemberJpaModel(UUID id, UUID deckId, UUID userId, DeckRole role, Instant joinedAt) {
		this.id = id;
		this.deckId = deckId;
		this.userId = userId;
		this.role = role;
		this.joinedAt = joinedAt;
	}

	UUID getId() {
		return id;
	}

	UUID getDeckId() {
		return deckId;
	}

	UUID getUserId() {
		return userId;
	}

	DeckRole getRole() {
		return role;
	}

	Instant getJoinedAt() {
		return joinedAt;
	}
}
