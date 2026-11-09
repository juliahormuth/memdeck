package br.com.memdeck.deck.domain;

public class InvalidDeckException extends RuntimeException {

	public InvalidDeckException(String message) {
		super(message);
	}
}
