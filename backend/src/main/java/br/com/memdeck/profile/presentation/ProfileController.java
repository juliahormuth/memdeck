package br.com.memdeck.profile.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.profile.application.GetProfileUseCase;
import br.com.memdeck.profile.application.UpdateProfileUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

	private final GetProfileUseCase getProfile;
	private final UpdateProfileUseCase updateProfile;

	public ProfileController(GetProfileUseCase getProfile, UpdateProfileUseCase updateProfile) {
		this.getProfile = getProfile;
		this.updateProfile = updateProfile;
	}

	@GetMapping
	public ProfileResponse show(@AuthenticationPrincipal AuthenticatedUser user) {
		return ProfileResponse.from(getProfile.execute(user.id()));
	}

	@PutMapping
	public ProfileResponse update(@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody UpdateProfileRequest request) {
		updateProfile.execute(request.toCommand(user.id()));
		return ProfileResponse.from(getProfile.execute(user.id()));
	}
}
