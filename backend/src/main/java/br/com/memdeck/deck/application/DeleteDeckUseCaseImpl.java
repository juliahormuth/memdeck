package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteDeckUseCaseImpl implements DeleteDeckUseCase {

	private final DeckAccessFinder access;
	private final DeckGateway gateway;

	public DeleteDeckUseCaseImpl(DeckAccessFinder access, DeckGateway gateway) {
		this.access = access;
		this.gateway = gateway;
	}

	@Override
	public void execute(UUID deckId, UUID requesterId) {
		gateway.deleteById(access.owned(deckId, requesterId).deck().getId());
	}
}
