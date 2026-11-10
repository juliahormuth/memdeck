package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class UserJpaModel {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 254, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserJpaModel() {
	}

	UserJpaModel(UUID id, String name, String email, String passwordHash, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
