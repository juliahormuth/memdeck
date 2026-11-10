package br.com.memdeck.user.presentation;

import java.util.UUID;

public record CurrentUserResponse(UUID id, String email) {
}
