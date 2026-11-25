package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.user.domain.PasswordReset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_resets")
class PasswordResetJpaModel {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "code_hash", nullable = false, length = 100)
	private String codeHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "failed_attempts", nullable = false)
	private int failedAttempts;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "window_started_at", nullable = false)
	private Instant windowStartedAt;

	@Column(name = "codes_in_window", nullable = false)
	private int codesInWindow;

	protected PasswordResetJpaModel() {
	}

	static PasswordResetJpaModel from(PasswordReset reset) {
		PasswordResetJpaModel model = new PasswordResetJpaModel();
		model.userId = reset.userId();
		model.codeHash = reset.codeHash();
		model.expiresAt = reset.expiresAt();
		model.failedAttempts = reset.failedAttempts();
		model.requestedAt = reset.requestedAt();
		model.windowStartedAt = reset.windowStartedAt();
		model.codesInWindow = reset.codesInWindow();
		return model;
	}

	PasswordReset toDomain() {
		return new PasswordReset(userId, codeHash, expiresAt, failedAttempts, requestedAt, windowStartedAt,
				codesInWindow);
	}
}
