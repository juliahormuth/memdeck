package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckRole;

public record DeckAccess(Deck deck, DeckRole role) {

	public DeckView toView() {
		return new DeckView(deck, role);
	}
}
