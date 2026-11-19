package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.AlreadyDeckMemberException;
import br.com.memdeck.deck.domain.DeckMember;

@Service
@Transactional
public class JoinDeckUseCaseImpl implements JoinDeckUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;

	public JoinDeckUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members) {
		this.access = access;
		this.members = members;
	}

	@Override
	public DeckView execute(UUID deckId, UUID requesterId) {
		DeckAccess found = access.viewable(deckId, requesterId);
		if (found.role() != null) {
			throw new AlreadyDeckMemberException(deckId);
		}
		DeckMember member = members.save(DeckMember.joinAsReader(deckId, requesterId));
		return new DeckView(found.deck(), member.getRole());
	}
}
