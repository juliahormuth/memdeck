package br.com.memdeck.invitation.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.invitation.domain.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deck_invitations")
class InvitationJpaModel {

	@Id
	private UUID id;

	@Column(name = "deck_id", nullable = false)
	private UUID deckId;

	@Column(name = "invitee_id", nullable = false)
	private UUID inviteeId;

	@Column(name = "inviter_id", nullable = false)
	private UUID inviterId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private DeckRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private InvitationStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "responded_at")
	private Instant respondedAt;

	protected InvitationJpaModel() {
	}

	InvitationJpaModel(UUID id, UUID deckId, UUID inviteeId, UUID inviterId, DeckRole role, InvitationStatus status,
			Instant createdAt, Instant respondedAt) {
		this.id = id;
		this.deckId = deckId;
		this.inviteeId = inviteeId;
		this.inviterId = inviterId;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
		this.respondedAt = respondedAt;
	}

	UUID getId() {
		return id;
	}

	UUID getDeckId() {
		return deckId;
	}

	UUID getInviteeId() {
		return inviteeId;
	}

	UUID getInviterId() {
		return inviterId;
	}

	DeckRole getRole() {
		return role;
	}

	InvitationStatus getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getRespondedAt() {
		return respondedAt;
	}
}
