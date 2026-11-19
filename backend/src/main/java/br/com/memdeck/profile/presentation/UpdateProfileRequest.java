package br.com.memdeck.profile.presentation;

import java.util.UUID;

import br.com.memdeck.profile.application.UpdateProfileCommand;
import br.com.memdeck.user.domain.Avatar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(@NotBlank @Size(max = 100) String name, @NotNull Avatar avatar) {

	UpdateProfileCommand toCommand(UUID userId) {
		return new UpdateProfileCommand(userId, name, avatar);
	}
}
