package br.com.memdeck.friendship.domain;

public class InvalidFriendRequestException extends RuntimeException {

	public InvalidFriendRequestException(String message) {
		super(message);
	}
}
