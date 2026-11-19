package br.com.memdeck.invitation.domain;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckRole;

public class Invitation {

	private final UUID id;
	private final UUID deckId;
	private final UUID inviteeId;
	private final UUID inviterId;
	private final DeckRole role;
	private final InvitationStatus status;
	private final Instant createdAt;
	private final Instant respondedAt;

	private Invitation(UUID id, UUID deckId, UUID inviteeId, UUID inviterId, DeckRole role, InvitationStatus status,
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

	public static Invitation create(UUID deckId, UUID inviterId, UUID inviteeId, DeckRole role) {
		requireOfferableRole(role);
		if (inviterId.equals(inviteeId)) {
			throw new InvalidInvitationException("You cannot invite yourself");
		}
		return new Invitation(UUID.randomUUID(), deckId, inviteeId, inviterId, role, InvitationStatus.PENDING,
				Instant.now(), null);
	}

	public static void requireOfferableRole(DeckRole role) {
		if (role != DeckRole.EDITOR && role != DeckRole.READER) {
			throw new InvalidInvitationException("An invitation must offer the editor or the reader role");
		}
	}

	public static Invitation restore(UUID id, UUID deckId, UUID inviteeId, UUID inviterId, DeckRole role,
			InvitationStatus status, Instant createdAt, Instant respondedAt) {
		return new Invitation(id, deckId, inviteeId, inviterId, role, status, createdAt, respondedAt);
	}

	public Invitation accept() {
		return answered(InvitationStatus.ACCEPTED);
	}

	public Invitation decline() {
		return answered(InvitationStatus.DECLINED);
	}

	private Invitation answered(InvitationStatus answer) {
		if (status != InvitationStatus.PENDING) {
			throw new InvitationNotPendingException(id);
		}
		return new Invitation(id, deckId, inviteeId, inviterId, role, answer, createdAt, Instant.now());
	}

	public boolean isPending() {
		return status == InvitationStatus.PENDING;
	}

	public UUID getId() {
		return id;
	}

	public UUID getDeckId() {
		return deckId;
	}

	public UUID getInviteeId() {
		return inviteeId;
	}

	public UUID getInviterId() {
		return inviterId;
	}

	public DeckRole getRole() {
		return role;
	}

	public InvitationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getRespondedAt() {
		return respondedAt;
	}
}
