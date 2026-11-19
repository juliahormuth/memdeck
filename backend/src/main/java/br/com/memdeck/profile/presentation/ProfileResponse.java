package br.com.memdeck.profile.presentation;

import java.util.UUID;

import br.com.memdeck.profile.application.ProfileView;
import br.com.memdeck.user.domain.Avatar;

public record ProfileResponse(UUID id, String name, String email, Avatar avatar, int reviewedDays, int streakDays,
		int friends) {

	static ProfileResponse from(ProfileView view) {
		return new ProfileResponse(view.user().getId(), view.user().getName(), view.user().getEmail(),
				view.user().getAvatar(), view.reviewedDays(), view.streakDays(), view.friends());
	}
}
