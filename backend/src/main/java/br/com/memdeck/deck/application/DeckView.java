package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckRole;

public record DeckView(Deck deck, DeckRole role) {

	public static DeckView ownedBy(Deck deck) {
		return new DeckView(deck, DeckRole.OWNER);
	}
}
