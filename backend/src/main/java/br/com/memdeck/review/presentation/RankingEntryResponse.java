package br.com.memdeck.review.presentation;

import java.util.UUID;

import br.com.memdeck.review.application.RankingEntry;
import br.com.memdeck.user.domain.Avatar;

public record RankingEntryResponse(int position, UUID userId, String name, Avatar avatar, long reviewedFlashcards,
		boolean me) {

	static RankingEntryResponse from(RankingEntry entry, UUID requesterId) {
		return new RankingEntryResponse(entry.position(), entry.userId(), entry.name(), entry.avatar(),
				entry.reviewedFlashcards(), entry.userId().equals(requesterId));
	}
}
