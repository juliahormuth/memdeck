package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_attempts")
class LoginAttemptJpaModel {

	@Id
	private UUID id;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(nullable = false, length = 64)
	private String origin;

	@Column(name = "attempted_at", nullable = false)
	private Instant attemptedAt;

	protected LoginAttemptJpaModel() {
	}

	LoginAttemptJpaModel(UUID id, String email, String origin, Instant attemptedAt) {
		this.id = id;
		this.email = email;
		this.origin = origin;
		this.attemptedAt = attemptedAt;
	}

	Instant getAttemptedAt() {
		return attemptedAt;
	}
}
