import type { DeckFieldErrors } from './rules';

export class InvalidDeckError extends Error {
	readonly fields: DeckFieldErrors;

	constructor(fields: DeckFieldErrors) {
		super('Invalid deck data');
		this.name = 'InvalidDeckError';
		this.fields = fields;
	}
}

export class DeckNotFoundError extends Error {
	constructor() {
		super('Deck not found');
		this.name = 'DeckNotFoundError';
	}
}
