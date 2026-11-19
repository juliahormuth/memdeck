package br.com.memdeck.deck.domain;

import java.util.UUID;

public class DeckMemberNotFoundException extends RuntimeException {

	public DeckMemberNotFoundException(UUID deckId, UUID userId) {
		super("User " + userId + " is not a member of deck " + deckId);
	}
}
