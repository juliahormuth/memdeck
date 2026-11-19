package br.com.memdeck.review.application;

import java.util.UUID;

import br.com.memdeck.user.domain.Avatar;

public record RankingEntry(int position, UUID userId, String name, Avatar avatar, long reviewedFlashcards) {
}
