package br.com.memdeck.review.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckParticipantException;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;

@RestControllerAdvice(assignableTypes = ReviewController.class)
class ReviewExceptionHandler {

	@ExceptionHandler(DeckNotFoundException.class)
	ProblemDetail handleDeckNotFound(DeckNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(FlashcardNotFoundException.class)
	ProblemDetail handleFlashcardNotFound(FlashcardNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(NotDeckParticipantException.class)
	ProblemDetail handleNotParticipant(NotDeckParticipantException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
	}
}
