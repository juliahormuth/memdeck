package br.com.memdeck.deck.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.deck.domain.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deck")
class DeckJpaModel {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Visibility visibility;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected DeckJpaModel() {
		// required by JPA
	}

	DeckJpaModel(UUID id, String name, String description, Visibility visibility, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	String getDescription() {
		return description;
	}

	Visibility getVisibility() {
		return visibility;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
