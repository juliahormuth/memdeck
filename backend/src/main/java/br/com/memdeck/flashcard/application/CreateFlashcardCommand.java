package br.com.memdeck.flashcard.application;

import java.util.UUID;

public record CreateFlashcardCommand(UUID requesterId, UUID deckId, String front, String back) {
}
