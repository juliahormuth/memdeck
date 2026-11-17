package br.com.memdeck.flashcard.application;

import java.util.UUID;

public record UpdateFlashcardCommand(UUID requesterId, UUID deckId, UUID flashcardId, String front, String back) {
}
