package br.com.memdeck.user.application;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.domain.InvalidPasswordResetCodeException;
import br.com.memdeck.user.domain.PasswordPolicy;
import br.com.memdeck.user.domain.PasswordReset;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(noRollbackFor = InvalidPasswordResetCodeException.class)
public class ConfirmPasswordResetUseCaseImpl implements ConfirmPasswordResetUseCase {

	private final UserGateway users;
	private final PasswordResetGateway resets;
	private final PasswordResetCodes codes;
	private final PasswordHasher passwordHasher;
	private final UserSessions sessions;
	private final LoginAttemptGateway attempts;
	private final Clock clock;

	public ConfirmPasswordResetUseCaseImpl(UserGateway users, PasswordResetGateway resets, PasswordResetCodes codes,
			PasswordHasher passwordHasher, UserSessions sessions, LoginAttemptGateway attempts, Clock clock) {
		this.users = users;
		this.resets = resets;
		this.codes = codes;
		this.passwordHasher = passwordHasher;
		this.sessions = sessions;
		this.attempts = attempts;
		this.clock = clock;
	}

	@Override
	public void execute(ConfirmPasswordResetCommand command) {
		PasswordPolicy.validate(command.newPassword());
		User user = users.findByEmail(User.normalizeEmail(command.email()))
				.orElseThrow(InvalidPasswordResetCodeException::new);
		PasswordReset reset = resets.findForUpdate(user.getId())
				.filter(found -> found.isUsableAt(clock.instant()))
				.orElseThrow(InvalidPasswordResetCodeException::new);
		if (!codes.matches(user.getId(), command.code(), reset.codeHash())) {
			resets.save(reset.withFailedAttempt());
			throw new InvalidPasswordResetCodeException();
		}
		users.save(user.withPasswordHash(passwordHasher.hash(command.newPassword())));
		resets.delete(user.getId());
		attempts.clearAll(user.getEmail());
		sessions.endAll(user.getId());
	}
}
