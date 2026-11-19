package br.com.memdeck.invitation.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.invitation.domain.InvalidInvitationException;
import br.com.memdeck.invitation.domain.InvitationNotFoundException;
import br.com.memdeck.invitation.domain.InvitationNotPendingException;

@RestControllerAdvice(assignableTypes = InvitationController.class)
class InvitationExceptionHandler {

	@ExceptionHandler({ DeckNotFoundException.class, InvitationNotFoundException.class })
	ProblemDetail handleNotFound(RuntimeException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(NotDeckOwnerException.class)
	ProblemDetail handleNotOwner(NotDeckOwnerException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(InvalidInvitationException.class)
	ProblemDetail handleInvalid(InvalidInvitationException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(InvitationNotPendingException.class)
	ProblemDetail handleNotPending(InvitationNotPendingException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
