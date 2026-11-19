package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetDeckUseCaseImpl implements GetDeckUseCase {

	private final DeckAccessFinder access;

	public GetDeckUseCaseImpl(DeckAccessFinder access) {
		this.access = access;
	}

	@Override
	public DeckView execute(UUID id, UUID requesterId) {
		return access.viewable(id, requesterId).toView();
	}
}
