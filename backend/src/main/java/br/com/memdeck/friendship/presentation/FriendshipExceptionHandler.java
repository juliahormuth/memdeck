package br.com.memdeck.friendship.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.memdeck.friendship.domain.FriendshipNotFoundException;
import br.com.memdeck.friendship.domain.InvalidFriendRequestException;
import br.com.memdeck.friendship.domain.TooManySearchesException;

@RestControllerAdvice(assignableTypes = { FriendshipController.class, UserSearchController.class })
class FriendshipExceptionHandler {

	@ExceptionHandler(FriendshipNotFoundException.class)
	ProblemDetail handleNotFound(FriendshipNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(TooManySearchesException.class)
	ResponseEntity<ProblemDetail> handleTooManySearches(TooManySearchesException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfter().toSeconds()))
				.body(body);
	}

	@ExceptionHandler(InvalidFriendRequestException.class)
	ProblemDetail handleInvalid(InvalidFriendRequestException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
