package br.com.memdeck.friendship.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.friendship.domain.FriendshipNotFoundException;
import br.com.memdeck.friendship.domain.InvalidFriendRequestException;

@RestControllerAdvice(assignableTypes = FriendshipController.class)
class FriendshipExceptionHandler {

	@ExceptionHandler(FriendshipNotFoundException.class)
	ProblemDetail handleNotFound(FriendshipNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(InvalidFriendRequestException.class)
	ProblemDetail handleInvalid(InvalidFriendRequestException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
