package br.com.memdeck.profile.application;

import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record UpdateProfileCommand(UUID userId, String name, Avatar avatar) {
}
