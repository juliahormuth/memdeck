package br.com.memdeck.invitation.domain;

import java.util.UUID;

public class InvitationNotPendingException extends RuntimeException {

	public InvitationNotPendingException(UUID id) {
		super("Invitation " + id + " was already answered");
	}
}
