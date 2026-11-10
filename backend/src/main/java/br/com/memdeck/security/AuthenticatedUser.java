package br.com.memdeck.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email) implements Principal, Serializable {

	@Override
	public String getName() {
		return id.toString();
	}
}
