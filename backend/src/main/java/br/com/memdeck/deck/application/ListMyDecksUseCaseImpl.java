package br.com.memdeck.deck.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;

@Service
@Transactional(readOnly = true)
public class ListMyDecksUseCaseImpl implements ListMyDecksUseCase {

	private final DeckGateway gateway;

	public ListMyDecksUseCaseImpl(DeckGateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public List<Deck> execute(UUID ownerId) {
		return gateway.findByOwnerNewestFirst(ownerId);
	}
}
