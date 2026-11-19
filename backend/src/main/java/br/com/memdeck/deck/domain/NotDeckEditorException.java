package br.com.memdeck.deck.domain;

import java.util.UUID;

public class NotDeckEditorException extends RuntimeException {

	public NotDeckEditorException(UUID deckId) {
		super("Only the owner or an editor can change flashcards of deck " + deckId);
	}
}
