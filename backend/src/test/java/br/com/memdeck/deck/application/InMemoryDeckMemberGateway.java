package br.com.memdeck.deck.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckMember;

class InMemoryDeckMemberGateway implements DeckMemberGateway {

	private final List<DeckMember> store = new ArrayList<>();

	@Override
	public DeckMember save(DeckMember member) {
		store.removeIf(existing -> existing.getId().equals(member.getId()));
		store.add(member);
		return member;
	}

	@Override
	public Optional<DeckMember> findByDeckAndUser(UUID deckId, UUID userId) {
		return store.stream()
				.filter(member -> member.getDeckId().equals(deckId) && member.getUserId().equals(userId))
				.findFirst();
	}

	@Override
	public List<DeckMember> findByUser(UUID userId) {
		return store.stream().filter(member -> member.getUserId().equals(userId)).toList();
	}

	int size() {
		return store.size();
	}
}
