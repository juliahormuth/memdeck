package br.com.memdeck.friendship.presentation;

import java.time.LocalDate;
import java.util.UUID;

import br.com.memdeck.friendship.application.FriendActivity;
import br.com.memdeck.user.domain.Avatar;

public record FriendActivityResponse(UUID userId, String name, Avatar avatar, LocalDate day, long reviewedFlashcards) {

	static FriendActivityResponse from(FriendActivity activity) {
		return new FriendActivityResponse(activity.userId(), activity.name(), activity.avatar(), activity.day(),
				activity.reviewedFlashcards());
	}
}
