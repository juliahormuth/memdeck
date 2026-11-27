package br.com.memdeck.friendship.application;

import java.util.Optional;
import java.util.UUID;

public interface SearchUserUseCase {

	Optional<FoundUser> execute(UUID requesterId, String username);
}
