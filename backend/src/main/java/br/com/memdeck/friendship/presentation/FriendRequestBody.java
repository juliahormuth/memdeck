package br.com.memdeck.friendship.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendRequestBody(@NotBlank @Size(max = 254) String email) {
}
