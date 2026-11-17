package br.com.memdeck.review.presentation;

import java.util.UUID;

import br.com.memdeck.review.application.StudyCard;

public record StudyCardResponse(UUID id, String front, String back, boolean newCard) {

	static StudyCardResponse from(StudyCard card) {
		return new StudyCardResponse(card.flashcard().getId(), card.flashcard().getFront(),
				card.flashcard().getBack(), card.newCard());
	}
}
