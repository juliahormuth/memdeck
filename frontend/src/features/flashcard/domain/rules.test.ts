import { describe, expect, it } from 'vitest';
import {
	FLASHCARD_BACK_MAX_LENGTH,
	FLASHCARD_FRONT_MAX_LENGTH,
	validateFlashcardBack,
	validateFlashcardFront,
	validateNewFlashcard,
} from './rules';

describe('flashcard rules', () => {
	it('requires a front and a back', () => {
		expect(validateFlashcardFront('   ')).toBe('blank');
		expect(validateFlashcardBack('')).toBe('blank');
	});

	it('accepts text with exactly the limits, ignoring surrounding spaces', () => {
		expect(validateFlashcardFront(` ${'a'.repeat(FLASHCARD_FRONT_MAX_LENGTH)} `)).toBeNull();
		expect(validateFlashcardBack('b'.repeat(FLASHCARD_BACK_MAX_LENGTH))).toBeNull();
	});

	it('rejects text over the limits', () => {
		expect(validateFlashcardFront('a'.repeat(FLASHCARD_FRONT_MAX_LENGTH + 1))).toBe('too_long');
		expect(validateFlashcardBack('b'.repeat(FLASHCARD_BACK_MAX_LENGTH + 1))).toBe('too_long');
	});

	it('reports every invalid field at once', () => {
		expect(validateNewFlashcard({ front: '', back: 'b'.repeat(FLASHCARD_BACK_MAX_LENGTH + 1) })).toEqual({
			front: 'blank',
			back: 'too_long',
		});
		expect(validateNewFlashcard({ front: 'Úmero', back: 'Osso do braço' })).toEqual({});
	});
});
