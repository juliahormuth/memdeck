package br.com.memdeck.invitation.domain;

public class InvalidInvitationException extends RuntimeException {

	public InvalidInvitationException(String message) {
		super(message);
	}
}
