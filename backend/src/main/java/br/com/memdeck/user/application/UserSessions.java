package br.com.memdeck.user.application;

import java.util.UUID;

public interface UserSessions {

	void endAll(UUID userId);
}
