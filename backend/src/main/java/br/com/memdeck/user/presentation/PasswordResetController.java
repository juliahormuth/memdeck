package br.com.memdeck.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.user.application.ConfirmPasswordResetUseCase;
import br.com.memdeck.user.application.RequestPasswordResetUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

	private final RequestPasswordResetUseCase requestPasswordReset;
	private final ConfirmPasswordResetUseCase confirmPasswordReset;

	public PasswordResetController(RequestPasswordResetUseCase requestPasswordReset,
			ConfirmPasswordResetUseCase confirmPasswordReset) {
		this.requestPasswordReset = requestPasswordReset;
		this.confirmPasswordReset = confirmPasswordReset;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void request(@Valid @RequestBody PasswordResetRequest request) {
		requestPasswordReset.execute(request.email());
	}

	@PostMapping("/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@Valid @RequestBody ConfirmPasswordResetRequest request) {
		confirmPasswordReset.execute(request.toCommand());
	}
}
