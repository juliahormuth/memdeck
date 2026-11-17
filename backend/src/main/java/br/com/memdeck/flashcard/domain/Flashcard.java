package br.com.memdeck.flashcard.domain;

import java.time.Instant;
import java.util.UUID;

public class Flashcard {

	public static final int FRONT_MAX_LENGTH = 500;
	public static final int BACK_MAX_LENGTH = 10000;

	private final UUID id;
	private final UUID deckId;
	private final String front;
	private final String back;
	private final Instant createdAt;

	private Flashcard(UUID id, UUID deckId, String front, String back, Instant createdAt) {
		this.id = id;
		this.deckId = deckId;
		this.front = front;
		this.back = back;
		this.createdAt = createdAt;
	}

	public static Flashcard create(UUID deckId, String front, String back) {
		if (deckId == null) {
			throw new InvalidFlashcardException("Flashcard deck is required");
		}
		String trimmedFront = requiredText(front, "front", FRONT_MAX_LENGTH);
		String trimmedBack = requiredText(back, "back", BACK_MAX_LENGTH);
		return new Flashcard(UUID.randomUUID(), deckId, trimmedFront, trimmedBack, Instant.now());
	}

	public Flashcard withChanges(String newFront, String newBack) {
		return new Flashcard(id, deckId, requiredText(newFront, "front", FRONT_MAX_LENGTH),
				requiredText(newBack, "back", BACK_MAX_LENGTH), createdAt);
	}

	public static Flashcard restore(UUID id, UUID deckId, String front, String back, Instant createdAt) {
		return new Flashcard(id, deckId, front, back, createdAt);
	}

	private static String requiredText(String value, String field, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new InvalidFlashcardException("Flashcard " + field + " must not be blank");
		}
		String trimmed = value.strip();
		if (trimmed.length() > maxLength) {
			throw new InvalidFlashcardException(
					"Flashcard " + field + " must have at most " + maxLength + " characters");
		}
		return trimmed;
	}

	public UUID getId() {
		return id;
	}

	public UUID getDeckId() {
		return deckId;
	}

	public String getFront() {
		return front;
	}

	public String getBack() {
		return back;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
