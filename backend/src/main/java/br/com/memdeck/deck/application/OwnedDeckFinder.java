package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;

@Component
public class OwnedDeckFinder {

	private final DeckGateway gateway;

	public OwnedDeckFinder(DeckGateway gateway) {
		this.gateway = gateway;
	}

	public Deck find(UUID deckId, UUID requesterId) {
		Deck deck = gateway.findById(deckId)
				.filter(found -> found.canBeViewedBy(requesterId))
				.orElseThrow(() -> new DeckNotFoundException(deckId));
		if (!deck.isOwnedBy(requesterId)) {
			throw new NotDeckOwnerException(deckId);
		}
		return deck;
	}
}
