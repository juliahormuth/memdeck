package br.com.memdeck.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.user.domain.EmailAlreadyRegisteredException;
import br.com.memdeck.user.domain.InvalidUserException;

@RestControllerAdvice(assignableTypes = UserController.class)
class UserExceptionHandler {

	@ExceptionHandler(InvalidUserException.class)
	ProblemDetail handleInvalid(InvalidUserException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ProblemDetail handleDuplicate(EmailAlreadyRegisteredException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
