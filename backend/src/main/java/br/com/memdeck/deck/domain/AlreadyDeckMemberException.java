package br.com.memdeck.deck.domain;

import java.util.UUID;

public class AlreadyDeckMemberException extends RuntimeException {

	public AlreadyDeckMemberException(UUID deckId) {
		super("User already takes part in deck " + deckId);
	}
}
