package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;

@Service
@Transactional(readOnly = true)
public class GetDeckUseCaseImpl implements GetDeckUseCase {

	private final DeckGateway gateway;
	private final DeckMemberGateway members;

	public GetDeckUseCaseImpl(DeckGateway gateway, DeckMemberGateway members) {
		this.gateway = gateway;
		this.members = members;
	}

	@Override
	public DeckView execute(UUID id, UUID requesterId) {
		Deck deck = gateway.findById(id).orElseThrow(() -> new DeckNotFoundException(id));
		if (!deck.canBeViewedBy(requesterId)) {
			throw new DeckNotFoundException(id);
		}
		if (deck.isOwnedBy(requesterId)) {
			return DeckView.ownedBy(deck);
		}
		DeckRole role = members.findByDeckAndUser(id, requesterId).map(DeckMember::getRole).orElse(null);
		return new DeckView(deck, role);
	}
}
