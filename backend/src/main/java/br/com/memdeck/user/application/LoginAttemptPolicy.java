package br.com.memdeck.user.application;

import java.time.Duration;

public record LoginAttemptPolicy(int maxFailures, Duration window) {
}
