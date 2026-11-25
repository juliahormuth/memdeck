package br.com.memdeck.user.infrastructure;

import java.util.UUID;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import br.com.memdeck.user.application.UserSessions;

@Component
class JdbcUserSessions implements UserSessions {

	private final FindByIndexNameSessionRepository<? extends Session> repository;

	JdbcUserSessions(FindByIndexNameSessionRepository<? extends Session> repository) {
		this.repository = repository;
	}

	@Override
	public void endAll(UUID userId) {
		repository.findByPrincipalName(userId.toString()).keySet().forEach(repository::deleteById);
	}
}
