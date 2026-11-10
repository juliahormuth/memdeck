package br.com.memdeck.user.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SessionAuthenticator;
import br.com.memdeck.user.application.AuthenticateUserUseCase;
import br.com.memdeck.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticateUserUseCase authenticateUser;
	private final SessionAuthenticator sessionAuthenticator;

	public AuthController(AuthenticateUserUseCase authenticateUser, SessionAuthenticator sessionAuthenticator) {
		this.authenticateUser = authenticateUser;
		this.sessionAuthenticator = sessionAuthenticator;
	}

	@PostMapping("/login")
	public CurrentUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		User user = authenticateUser.execute(request.toCommand(httpRequest.getRemoteAddr()));
		sessionAuthenticator.signIn(new AuthenticatedUser(user.getId(), user.getEmail()), httpRequest, httpResponse);
		return new CurrentUserResponse(user.getId(), user.getEmail());
	}

	@GetMapping("/me")
	public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
		return new CurrentUserResponse(user.id(), user.email());
	}
}
