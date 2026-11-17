import type { FlashcardFieldErrors } from './rules';

export class InvalidFlashcardError extends Error {
	readonly fields: FlashcardFieldErrors;

	constructor(fields: FlashcardFieldErrors) {
		super('Invalid flashcard data');
		this.name = 'InvalidFlashcardError';
		this.fields = fields;
	}
}

export class FlashcardDeckNotFoundError extends Error {
	constructor() {
		super('Deck not found');
		this.name = 'FlashcardDeckNotFoundError';
	}
}

export class FlashcardNotFoundError extends Error {
	constructor() {
		super('Flashcard not found');
		this.name = 'FlashcardNotFoundError';
	}
}

export class NotDeckOwnerError extends Error {
	constructor() {
		super('Only the deck owner can change it');
		this.name = 'NotDeckOwnerError';
	}
}
