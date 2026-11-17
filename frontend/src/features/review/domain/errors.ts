export class StudyDeckNotFoundError extends Error {
	constructor() {
		super('Deck not found');
		this.name = 'StudyDeckNotFoundError';
	}
}

export class StudyFlashcardNotFoundError extends Error {
	constructor() {
		super('Flashcard not found');
		this.name = 'StudyFlashcardNotFoundError';
	}
}
