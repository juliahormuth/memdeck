package br.com.memdeck.deck.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckEditorException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;

@Component
public class DeckAccessFinder {

	private final DeckGateway decks;
	private final DeckMemberGateway members;

	public DeckAccessFinder(DeckGateway decks, DeckMemberGateway members) {
		this.decks = decks;
		this.members = members;
	}

	// 404 when the user cannot see the deck (same answer as a missing deck); 403 when they see it but lack the right.
	public DeckAccess viewable(UUID deckId, UUID userId) {
		Deck deck = decks.findById(deckId).orElseThrow(() -> new DeckNotFoundException(deckId));
		DeckRole role = roleOf(deck, userId);
		if (role == null && !deck.canBeViewedBy(userId)) {
			throw new DeckNotFoundException(deckId);
		}
		return new DeckAccess(deck, role);
	}

	public DeckAccess editable(UUID deckId, UUID userId) {
		DeckAccess access = viewable(deckId, userId);
		if (access.role() != DeckRole.OWNER && access.role() != DeckRole.EDITOR) {
			throw new NotDeckEditorException(deckId);
		}
		return access;
	}

	public DeckAccess owned(UUID deckId, UUID userId) {
		DeckAccess access = viewable(deckId, userId);
		if (access.role() != DeckRole.OWNER) {
			throw new NotDeckOwnerException(deckId);
		}
		return access;
	}

	private DeckRole roleOf(Deck deck, UUID userId) {
		if (deck.isOwnedBy(userId)) {
			return DeckRole.OWNER;
		}
		return members.findByDeckAndUser(deck.getId(), userId).map(DeckMember::getRole).orElse(null);
	}
}
