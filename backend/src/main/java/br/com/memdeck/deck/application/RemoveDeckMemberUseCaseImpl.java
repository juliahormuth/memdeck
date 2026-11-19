package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckMemberNotFoundException;

@Service
@Transactional
public class RemoveDeckMemberUseCaseImpl implements RemoveDeckMemberUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;

	public RemoveDeckMemberUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members) {
		this.access = access;
		this.members = members;
	}

	@Override
	public void execute(UUID deckId, UUID memberUserId, UUID requesterId) {
		access.owned(deckId, requesterId);
		DeckMember member = members.findByDeckAndUser(deckId, memberUserId)
				.orElseThrow(() -> new DeckMemberNotFoundException(deckId, memberUserId));
		members.deleteById(member.getId());
	}
}
