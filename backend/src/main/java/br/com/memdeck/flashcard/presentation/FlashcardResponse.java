package br.com.memdeck.flashcard.presentation;

import java.time.Instant;
import java.util.UUID;

import br.com.memdeck.flashcard.domain.Flashcard;

public record FlashcardResponse(UUID id, UUID deckId, String front, String back, Instant createdAt) {

	static FlashcardResponse from(Flashcard flashcard) {
		return new FlashcardResponse(
				flashcard.getId(),
				flashcard.getDeckId(),
				flashcard.getFront(),
				flashcard.getBack(),
				flashcard.getCreatedAt());
	}
}
