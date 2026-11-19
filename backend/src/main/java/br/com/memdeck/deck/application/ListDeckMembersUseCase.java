package br.com.memdeck.deck.application;

import java.util.List;
import java.util.UUID;

public interface ListDeckMembersUseCase {

	List<DeckMemberView> execute(UUID deckId, UUID requesterId);
}
