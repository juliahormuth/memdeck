package br.com.memdeck.profile.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.UserNotFoundException;

@RestControllerAdvice(assignableTypes = ProfileController.class)
class ProfileExceptionHandler {

	@ExceptionHandler(InvalidUserException.class)
	ProblemDetail handleInvalid(InvalidUserException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	ProblemDetail handleNotFound(UserNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}
}
