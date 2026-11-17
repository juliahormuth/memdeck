package br.com.memdeck.deck.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.deck.domain.DeckMember;

public interface DeckMemberGateway {

	DeckMember save(DeckMember member);

	Optional<DeckMember> findByDeckAndUser(UUID deckId, UUID userId);

	List<DeckMember> findByUser(UUID userId);
}
