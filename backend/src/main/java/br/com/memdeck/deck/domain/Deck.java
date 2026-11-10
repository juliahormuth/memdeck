package br.com.memdeck.deck.domain;

import java.time.Instant;
import java.util.UUID;

public class Deck {

	public static final int NAME_MAX_LENGTH = 100;
	public static final int DESCRIPTION_MAX_LENGTH = 500;

	private final UUID id;
	private final String name;
	private final String description;
	private final Visibility visibility;
	private final Instant createdAt;

	private Deck(UUID id, String name, String description, Visibility visibility, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.createdAt = createdAt;
	}

	public static Deck create(String name, String description, Visibility visibility) {
		if (name == null || name.isBlank()) {
			throw new InvalidDeckException("Deck name must not be blank");
		}
		String trimmedName = name.strip();
		if (trimmedName.length() > NAME_MAX_LENGTH) {
			throw new InvalidDeckException("Deck name must have at most " + NAME_MAX_LENGTH + " characters");
		}
		if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
			throw new InvalidDeckException(
					"Deck description must have at most " + DESCRIPTION_MAX_LENGTH + " characters");
		}
		if (visibility == null) {
			throw new InvalidDeckException("Deck visibility is required");
		}
		return new Deck(UUID.randomUUID(), trimmedName, description, visibility, Instant.now());
	}

	public static Deck restore(UUID id, String name, String description, Visibility visibility, Instant createdAt) {
		return new Deck(id, name, description, visibility, createdAt);
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
