package br.com.memdeck.profile.application;

import java.util.UUID;

public interface GetProfileUseCase {

	ProfileView execute(UUID userId);
}
