package br.com.memdeck.security;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionAuthenticator {

	private final SecurityContextRepository securityContextRepository;
	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
	private final SecurityContextHolderStrategy holderStrategy = SecurityContextHolder.getContextHolderStrategy();

	SessionAuthenticator(SecurityContextRepository securityContextRepository,
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.securityContextRepository = securityContextRepository;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	public void signIn(AuthenticatedUser user, HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
				user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
		sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
		SecurityContext context = holderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		holderStrategy.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}
}
