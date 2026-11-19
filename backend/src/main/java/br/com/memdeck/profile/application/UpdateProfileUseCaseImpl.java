package br.com.memdeck.profile.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;
import br.com.memdeck.user.domain.UserNotFoundException;

@Service
@Transactional
public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {

	private final UserGateway users;

	public UpdateProfileUseCaseImpl(UserGateway users) {
		this.users = users;
	}

	@Override
	public User execute(UpdateProfileCommand command) {
		User user = users.findById(command.userId()).orElseThrow(() -> new UserNotFoundException(command.userId()));
		return users.save(user.withProfile(command.name(), command.avatar()));
	}
}
