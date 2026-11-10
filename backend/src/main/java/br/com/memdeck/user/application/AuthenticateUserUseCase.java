package br.com.memdeck.user.application;

import br.com.memdeck.user.domain.User;

public interface AuthenticateUserUseCase {

	User execute(AuthenticateUserCommand command);
}
