package br.com.memdeck.friendship.application;

import java.time.LocalDate;
import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record FriendActivity(UUID userId, String name, Avatar avatar, LocalDate day, long reviewedFlashcards) {
}
