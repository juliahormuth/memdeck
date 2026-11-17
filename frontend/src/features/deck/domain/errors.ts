import type { DeckFieldErrors } from './rules';

export class InvalidDeckError extends Error {
	readonly fields: DeckFieldErrors;

	constructor(fields: DeckFieldErrors) {
		super('Invalid deck data');
		this.name = 'InvalidDeckError';
		this.fields = fields;
	}
}

export class NotDeckOwnerError extends Error {
	constructor() {
		super('Only the deck owner can change it');
		this.name = 'NotDeckOwnerError';
	}
}

export class DeckNotFoundError extends Error {
	constructor() {
		super('Deck not found');
		this.name = 'DeckNotFoundError';
	}
}

export class AlreadyDeckMemberError extends Error {
	constructor() {
		super('User already takes part in this deck');
		this.name = 'AlreadyDeckMemberError';
	}
}
