package br.com.memdeck.flashcard.domain;

public class InvalidFlashcardException extends RuntimeException {

	public InvalidFlashcardException(String message) {
		super(message);
	}
}
