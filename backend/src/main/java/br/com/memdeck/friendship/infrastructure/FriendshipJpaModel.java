package br.com.memdeck.friendship.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.friendship.domain.FriendshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "friendships")
class FriendshipJpaModel {

	@Id
	private UUID id;

	@Column(name = "requester_id", nullable = false)
	private UUID requesterId;

	@Column(name = "addressee_id", nullable = false)
	private UUID addresseeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private FriendshipStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "responded_at")
	private Instant respondedAt;

	protected FriendshipJpaModel() {
	}

	FriendshipJpaModel(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status, Instant createdAt,
			Instant respondedAt) {
		this.id = id;
		this.requesterId = requesterId;
		this.addresseeId = addresseeId;
		this.status = status;
		this.createdAt = createdAt;
		this.respondedAt = respondedAt;
	}

	UUID getId() {
		return id;
	}

	UUID getRequesterId() {
		return requesterId;
	}

	UUID getAddresseeId() {
		return addresseeId;
	}

	FriendshipStatus getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getRespondedAt() {
		return respondedAt;
	}
}
