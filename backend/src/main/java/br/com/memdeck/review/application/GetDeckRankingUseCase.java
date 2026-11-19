package br.com.memdeck.review.application;

import java.util.List;
import java.util.UUID;

public interface GetDeckRankingUseCase {

	List<RankingEntry> execute(UUID deckId, UUID requesterId);
}
