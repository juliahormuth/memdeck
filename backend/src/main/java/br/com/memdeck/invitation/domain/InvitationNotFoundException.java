package br.com.memdeck.invitation.domain;

import java.util.UUID;

public class InvitationNotFoundException extends RuntimeException {

	public InvitationNotFoundException(UUID id) {
		super("Invitation " + id + " not found");
	}
}
