package br.com.memdeck.user.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record PasswordReset(UUID userId, String codeHash, Instant expiresAt, int failedAttempts, Instant requestedAt,
		Instant windowStartedAt, int codesInWindow) {

	public static final Duration CODE_LIFETIME = Duration.ofMinutes(15);
	public static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
	public static final Duration CODE_WINDOW = Duration.ofHours(24);
	public static final int MAX_FAILED_ATTEMPTS = 5;
	public static final int MAX_CODES_PER_WINDOW = 5;

	public static PasswordReset first(UUID userId, String codeHash, Instant now) {
		return new PasswordReset(userId, codeHash, now.plus(CODE_LIFETIME), 0, now, now, 1);
	}

	// together with MAX_FAILED_ATTEMPTS this caps how many guesses a 6-digit code space gets per day
	public boolean canIssueAt(Instant now) {
		boolean cooledDown = !now.isBefore(requestedAt.plus(RESEND_COOLDOWN));
		return cooledDown && (windowEndedAt(now) || codesInWindow < MAX_CODES_PER_WINDOW);
	}

	public PasswordReset reissue(String newCodeHash, Instant now) {
		boolean newWindow = windowEndedAt(now);
		return new PasswordReset(userId, newCodeHash, now.plus(CODE_LIFETIME), 0, now,
				newWindow ? now : windowStartedAt, newWindow ? 1 : codesInWindow + 1);
	}

	public boolean isUsableAt(Instant now) {
		return failedAttempts < MAX_FAILED_ATTEMPTS && now.isBefore(expiresAt);
	}

	public PasswordReset withFailedAttempt() {
		return new PasswordReset(userId, codeHash, expiresAt, failedAttempts + 1, requestedAt, windowStartedAt,
				codesInWindow);
	}

	private boolean windowEndedAt(Instant now) {
		return !now.isBefore(windowStartedAt.plus(CODE_WINDOW));
	}
}
