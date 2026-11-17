package br.com.memdeck.flashcard.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.flashcard.domain.InvalidFlashcardException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;

@RestControllerAdvice(assignableTypes = FlashcardController.class)
class FlashcardExceptionHandler {

	@ExceptionHandler(DeckNotFoundException.class)
	ProblemDetail handleDeckNotFound(DeckNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(FlashcardNotFoundException.class)
	ProblemDetail handleFlashcardNotFound(FlashcardNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(NotDeckOwnerException.class)
	ProblemDetail handleNotOwner(NotDeckOwnerException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(InvalidFlashcardException.class)
	ProblemDetail handleInvalid(InvalidFlashcardException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
