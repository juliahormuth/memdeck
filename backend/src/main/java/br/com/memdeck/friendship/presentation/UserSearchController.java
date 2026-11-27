package br.com.memdeck.friendship.presentation;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.friendship.application.SearchUserUseCase;
import br.com.memdeck.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/users")
public class UserSearchController {

	private final SearchUserUseCase searchUser;

	public UserSearchController(SearchUserUseCase searchUser) {
		this.searchUser = searchUser;
	}

	// only an exact username matches, so the list has at most one person
	@GetMapping
	public List<FoundUserResponse> search(@AuthenticationPrincipal AuthenticatedUser user,
			@RequestParam String username) {
		return searchUser.execute(user.id(), username).map(FoundUserResponse::from).stream().toList();
	}
}
