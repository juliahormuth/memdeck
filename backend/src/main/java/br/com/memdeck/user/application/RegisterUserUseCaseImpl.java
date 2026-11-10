package br.com.memdeck.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.domain.EmailAlreadyRegisteredException;
import br.com.memdeck.user.domain.PasswordPolicy;
import br.com.memdeck.user.domain.User;

@Service
@Transactional
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

	private final UserGateway gateway;
	private final PasswordHasher passwordHasher;

	public RegisterUserUseCaseImpl(UserGateway gateway, PasswordHasher passwordHasher) {
		this.gateway = gateway;
		this.passwordHasher = passwordHasher;
	}

	@Override
	public User execute(RegisterUserCommand command) {
		PasswordPolicy.validate(command.password());
		String email = User.normalizeEmail(command.email());
		if (gateway.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException();
		}
		User user = User.register(command.name(), email, passwordHasher.hash(command.password()));
		return gateway.save(user);
	}
}
