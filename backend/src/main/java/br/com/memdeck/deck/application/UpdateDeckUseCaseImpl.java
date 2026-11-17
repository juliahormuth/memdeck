package br.com.memdeck.deck.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;

@Service
@Transactional
public class UpdateDeckUseCaseImpl implements UpdateDeckUseCase {

	private final OwnedDeckFinder ownedDeck;
	private final DeckGateway gateway;

	public UpdateDeckUseCaseImpl(OwnedDeckFinder ownedDeck, DeckGateway gateway) {
		this.ownedDeck = ownedDeck;
		this.gateway = gateway;
	}

	@Override
	public Deck execute(UpdateDeckCommand command) {
		Deck deck = ownedDeck.find(command.deckId(), command.requesterId());
		return gateway.save(deck.withChanges(command.name(), command.description(), command.visibility()));
	}
}
