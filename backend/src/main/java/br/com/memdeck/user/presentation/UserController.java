package br.com.memdeck.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.user.application.RegisterUserUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final RegisterUserUseCase registerUser;

	public UserController(RegisterUserUseCase registerUser) {
		this.registerUser = registerUser;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
		return UserResponse.from(registerUser.execute(request.toCommand()));
	}
}
