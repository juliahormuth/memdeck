package br.com.memdeck.user.domain;

import java.time.Duration;

public class TooManyLoginAttemptsException extends RuntimeException {

	private final Duration retryAfter;

	public TooManyLoginAttemptsException(Duration retryAfter) {
		super("Too many failed login attempts. Try again later");
		this.retryAfter = retryAfter;
	}

	public Duration getRetryAfter() {
		return retryAfter;
	}
}
