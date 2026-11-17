export const FLASHCARD_FRONT_MAX_LENGTH = 500;
export const FLASHCARD_BACK_MAX_LENGTH = 10000;

export type FlashcardField = 'front' | 'back';
export type FlashcardFieldErrorCode = 'blank' | 'too_long';
export type FlashcardFieldErrors = Partial<Record<FlashcardField, FlashcardFieldErrorCode>>;

function validateText(text: string, maxLength: number): FlashcardFieldErrorCode | null {
	const trimmed = text.trim();
	if (trimmed.length === 0) {
		return 'blank';
	}
	return trimmed.length > maxLength ? 'too_long' : null;
}

export function validateFlashcardFront(front: string): FlashcardFieldErrorCode | null {
	return validateText(front, FLASHCARD_FRONT_MAX_LENGTH);
}

export function validateFlashcardBack(back: string): FlashcardFieldErrorCode | null {
	return validateText(back, FLASHCARD_BACK_MAX_LENGTH);
}

export function validateNewFlashcard(flashcard: { front: string; back: string }): FlashcardFieldErrors {
	const errors: FlashcardFieldErrors = {};
	const front = validateFlashcardFront(flashcard.front);
	const back = validateFlashcardBack(flashcard.back);
	if (front) errors.front = front;
	if (back) errors.back = back;
	return errors;
}
