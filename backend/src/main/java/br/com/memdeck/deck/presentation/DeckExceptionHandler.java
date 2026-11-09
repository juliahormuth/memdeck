package br.com.memdeck.deck.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.InvalidDeckException;

@RestControllerAdvice(assignableTypes = DeckController.class)
class DeckExceptionHandler {

	@ExceptionHandler(DeckNotFoundException.class)
	ProblemDetail handleNotFound(DeckNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(InvalidDeckException.class)
	ProblemDetail handleInvalid(InvalidDeckException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
