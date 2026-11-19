package br.com.memdeck.deck.domain;

import java.util.UUID;

public class NotDeckParticipantException extends RuntimeException {

	public NotDeckParticipantException(UUID deckId) {
		super("Only the participants of deck " + deckId + " can see its ranking");
	}
}
