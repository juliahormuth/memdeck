package br.com.memdeck.deck.application;

import java.util.List;
import java.util.UUID;

public interface SearchPublicDecksUseCase {

	List<DeckView> execute(UUID requesterId, String term);
}
