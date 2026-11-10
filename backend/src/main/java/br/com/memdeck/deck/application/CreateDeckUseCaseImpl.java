package br.com.memdeck.deck.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;

@Service
@Transactional
public class CreateDeckUseCaseImpl implements CreateDeckUseCase {

	private final DeckGateway gateway;

	public CreateDeckUseCaseImpl(DeckGateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public Deck execute(CreateDeckCommand command) {
		Deck deck = Deck.create(command.name(), command.description(), command.visibility());
		return gateway.save(deck);
	}
}
