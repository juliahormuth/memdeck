package br.com.memdeck.profile.application;

import br.com.memdeck.user.domain.User;

public record ProfileView(User user, int reviewedDays, int streakDays, int friends) {
}
