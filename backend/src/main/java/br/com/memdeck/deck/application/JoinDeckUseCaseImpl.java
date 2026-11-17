package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.AlreadyDeckMemberException;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;

@Service
@Transactional
public class JoinDeckUseCaseImpl implements JoinDeckUseCase {

	private final DeckGateway decks;
	private final DeckMemberGateway members;

	public JoinDeckUseCaseImpl(DeckGateway decks, DeckMemberGateway members) {
		this.decks = decks;
		this.members = members;
	}

	@Override
	public DeckView execute(UUID deckId, UUID requesterId) {
		Deck deck = decks.findById(deckId)
				.filter(found -> found.canBeViewedBy(requesterId))
				.orElseThrow(() -> new DeckNotFoundException(deckId));
		if (deck.isOwnedBy(requesterId) || members.findByDeckAndUser(deckId, requesterId).isPresent()) {
			throw new AlreadyDeckMemberException(deckId);
		}
		DeckMember member = members.save(DeckMember.joinAsReader(deckId, requesterId));
		return new DeckView(deck, member.getRole());
	}
}
