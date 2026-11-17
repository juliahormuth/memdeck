package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteDeckUseCaseImpl implements DeleteDeckUseCase {

	private final OwnedDeckFinder ownedDeck;
	private final DeckGateway gateway;

	public DeleteDeckUseCaseImpl(OwnedDeckFinder ownedDeck, DeckGateway gateway) {
		this.ownedDeck = ownedDeck;
		this.gateway = gateway;
	}

	@Override
	public void execute(UUID deckId, UUID requesterId) {
		gateway.deleteById(ownedDeck.find(deckId, requesterId).getId());
	}
}
