package br.com.memdeck.user.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.user.domain.PasswordReset;
import br.com.memdeck.user.domain.User;

@Service
@Transactional
public class RequestPasswordResetUseCaseImpl implements RequestPasswordResetUseCase {

	private final UserGateway users;
	private final PasswordResetGateway resets;
	private final PasswordResetCodes codes;
	private final PasswordResetMailer mailer;
	private final Clock clock;

	public RequestPasswordResetUseCaseImpl(UserGateway users, PasswordResetGateway resets, PasswordResetCodes codes,
			PasswordResetMailer mailer, Clock clock) {
		this.users = users;
		this.resets = resets;
		this.codes = codes;
		this.mailer = mailer;
		this.clock = clock;
	}

	// every outcome looks the same to the caller, so the endpoint never says which emails have an account
	@Override
	public void execute(String email) {
		users.findByEmail(User.normalizeEmail(email)).ifPresent(this::issueCode);
	}

	private void issueCode(User user) {
		Instant now = clock.instant();
		Optional<PasswordReset> current = resets.findForUpdate(user.getId());
		if (current.isPresent() && !current.get().canIssueAt(now)) {
			return;
		}
		String code = codes.generate();
		String codeHash = codes.hash(user.getId(), code);
		resets.save(current.map(reset -> reset.reissue(codeHash, now))
				.orElseGet(() -> PasswordReset.first(user.getId(), codeHash, now)));
		mailer.sendCode(user.getEmail(), code);
	}
}
