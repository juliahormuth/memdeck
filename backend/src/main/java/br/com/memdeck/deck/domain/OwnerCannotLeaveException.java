package br.com.memdeck.deck.domain;

import java.util.UUID;

public class OwnerCannotLeaveException extends RuntimeException {

	public OwnerCannotLeaveException(UUID deckId) {
		super("The owner cannot leave deck " + deckId + "; delete it instead");
	}
}
