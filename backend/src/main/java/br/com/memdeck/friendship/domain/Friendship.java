package br.com.memdeck.friendship.domain;

import java.time.Instant;
import java.util.UUID;

public class Friendship {

	private final UUID id;
	private final UUID requesterId;
	private final UUID addresseeId;
	private final FriendshipStatus status;
	private final Instant createdAt;
	private final Instant respondedAt;

	private Friendship(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status, Instant createdAt,
			Instant respondedAt) {
		this.id = id;
		this.requesterId = requesterId;
		this.addresseeId = addresseeId;
		this.status = status;
		this.createdAt = createdAt;
		this.respondedAt = respondedAt;
	}

	public static Friendship request(UUID requesterId, UUID addresseeId) {
		if (requesterId.equals(addresseeId)) {
			throw new InvalidFriendRequestException("You cannot send a friend request to yourself");
		}
		return new Friendship(UUID.randomUUID(), requesterId, addresseeId, FriendshipStatus.PENDING, Instant.now(),
				null);
	}

	public static Friendship restore(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status,
			Instant createdAt, Instant respondedAt) {
		return new Friendship(id, requesterId, addresseeId, status, createdAt, respondedAt);
	}

	public Friendship accept() {
		if (status != FriendshipStatus.PENDING) {
			throw new FriendshipNotFoundException(id);
		}
		return new Friendship(id, requesterId, addresseeId, FriendshipStatus.ACCEPTED, createdAt, Instant.now());
	}

	public boolean isPending() {
		return status == FriendshipStatus.PENDING;
	}

	public boolean involves(UUID userId) {
		return requesterId.equals(userId) || addresseeId.equals(userId);
	}

	public UUID otherUser(UUID userId) {
		return requesterId.equals(userId) ? addresseeId : requesterId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getRequesterId() {
		return requesterId;
	}

	public UUID getAddresseeId() {
		return addresseeId;
	}

	public FriendshipStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getRespondedAt() {
		return respondedAt;
	}
}
