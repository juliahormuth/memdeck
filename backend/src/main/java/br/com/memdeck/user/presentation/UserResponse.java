package br.com.memdeck.user.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.user.domain.User;

public record UserResponse(UUID id, String name, String username, String email, Instant createdAt) {

	static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail(), user.getCreatedAt());
	}
}
