package br.com.memdeck.deck.application;

import java.util.List;
import java.util.UUID;

public interface ListMyDecksUseCase {

	List<DeckView> execute(UUID userId);
}
