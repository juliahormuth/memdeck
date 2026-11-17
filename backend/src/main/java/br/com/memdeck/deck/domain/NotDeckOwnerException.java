package br.com.memdeck.deck.domain;

import java.util.UUID;

public class NotDeckOwnerException extends RuntimeException {

	public NotDeckOwnerException(UUID deckId) {
		super("Only the owner can change deck " + deckId);
	}
}
