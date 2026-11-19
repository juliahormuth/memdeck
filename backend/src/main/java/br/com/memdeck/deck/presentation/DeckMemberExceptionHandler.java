package br.com.memdeck.deck.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.deck.domain.DeckMemberNotFoundException;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.OwnerCannotLeaveException;

@RestControllerAdvice(assignableTypes = DeckMemberController.class)
class DeckMemberExceptionHandler {

	@ExceptionHandler({ DeckNotFoundException.class, DeckMemberNotFoundException.class })
	ProblemDetail handleNotFound(RuntimeException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(NotDeckOwnerException.class)
	ProblemDetail handleNotOwner(NotDeckOwnerException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(OwnerCannotLeaveException.class)
	ProblemDetail handleOwnerLeaving(OwnerCannotLeaveException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
