package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;

@Service
@Transactional(readOnly = true)
public class GetDeckUseCase {

	private final DeckGateway gateway;

	public GetDeckUseCase(DeckGateway gateway) {
		this.gateway = gateway;
	}

	public Deck execute(UUID id) {
		return gateway.findById(id).orElseThrow(() -> new DeckNotFoundException(id));
	}
}
