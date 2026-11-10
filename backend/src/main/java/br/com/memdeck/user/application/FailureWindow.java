package br.com.memdeck.user.application;

import java.time.Instant;
import java.util.Optional;

public record FailureWindow(int count, Optional<Instant> oldest) {
}
