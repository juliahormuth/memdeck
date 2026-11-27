package br.com.memdeck.friendship.domain;

import java.time.Duration;

public class TooManySearchesException extends RuntimeException {

	private final Duration retryAfter;

	public TooManySearchesException(Duration retryAfter) {
		super("Too many searches. Try again later");
		this.retryAfter = retryAfter;
	}

	public Duration getRetryAfter() {
		return retryAfter;
	}
}
