package br.com.memdeck.user.application;

import br.com.memdeck.user.domain.User;

public interface RegisterUserUseCase {

	User execute(RegisterUserCommand command);
}
