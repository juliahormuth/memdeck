package br.com.memdeck.deck.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;

@Service
@Transactional
public class UpdateDeckUseCaseImpl implements UpdateDeckUseCase {

	private final DeckAccessFinder access;
	private final DeckGateway gateway;

	public UpdateDeckUseCaseImpl(DeckAccessFinder access, DeckGateway gateway) {
		this.access = access;
		this.gateway = gateway;
	}

	@Override
	public Deck execute(UpdateDeckCommand command) {
		Deck deck = access.owned(command.deckId(), command.requesterId()).deck();
		return gateway.save(deck.withChanges(command.name(), command.description(), command.visibility()));
	}
}
