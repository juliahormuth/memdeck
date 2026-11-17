package br.com.memdeck.review.application;

import br.com.memdeck.flashcard.domain.Flashcard;

public record StudyCard(Flashcard flashcard, boolean newCard) {
}
