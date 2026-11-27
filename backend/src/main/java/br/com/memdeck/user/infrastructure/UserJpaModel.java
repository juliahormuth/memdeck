package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class UserJpaModel {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 20, unique = true)
	private String username;

	@Column(nullable = false, length = 254, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Avatar avatar;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserJpaModel() {
	}

	UserJpaModel(UUID id, String name, String username, String email, String passwordHash, Avatar avatar, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.avatar = avatar;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	String getUsername() {
		return username;
	}

	String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	Avatar getAvatar() {
		return avatar;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
