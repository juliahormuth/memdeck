package br.com.memdeck.profile.application;

import br.com.memdeck.user.domain.User;

public interface UpdateProfileUseCase {

	User execute(UpdateProfileCommand command);
}
