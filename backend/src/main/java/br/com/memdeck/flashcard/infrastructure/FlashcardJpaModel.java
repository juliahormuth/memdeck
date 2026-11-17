package br.com.memdeck.flashcard.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "flashcards")
class FlashcardJpaModel {

	@Id
	private UUID id;

	@Column(name = "deck_id", nullable = false)
	private UUID deckId;

	@Column(nullable = false, length = 500)
	private String front;

	@Column(nullable = false, length = 10000)
	private String back;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected FlashcardJpaModel() {
	}

	FlashcardJpaModel(UUID id, UUID deckId, String front, String back, Instant createdAt) {
		this.id = id;
		this.deckId = deckId;
		this.front = front;
		this.back = back;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	UUID getDeckId() {
		return deckId;
	}

	String getFront() {
		return front;
	}

	String getBack() {
		return back;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
