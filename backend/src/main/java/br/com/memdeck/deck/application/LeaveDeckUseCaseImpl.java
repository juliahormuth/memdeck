package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckMemberNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.OwnerCannotLeaveException;

@Service
@Transactional
public class LeaveDeckUseCaseImpl implements LeaveDeckUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;

	public LeaveDeckUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members) {
		this.access = access;
		this.members = members;
	}

	@Override
	public void execute(UUID deckId, UUID requesterId) {
		DeckAccess found = access.viewable(deckId, requesterId);
		if (found.role() == DeckRole.OWNER) {
			throw new OwnerCannotLeaveException(deckId);
		}
		DeckMember member = members.findByDeckAndUser(deckId, requesterId)
				.orElseThrow(() -> new DeckMemberNotFoundException(deckId, requesterId));
		members.deleteById(member.getId());
	}
}
