import { describe, expect, it } from 'vitest';
import {
	DECK_DESCRIPTION_MAX_LENGTH,
	DECK_NAME_MAX_LENGTH,
	validateDeckDescription,
	validateDeckName,
	validateNewDeck,
} from './rules';

describe('validateDeckName', () => {
	it('accepts a name and trims blanks', () => {
		expect(validateDeckName('  Anatomia  ')).toBeNull();
	});

	it('rejects a blank name', () => {
		expect(validateDeckName('   ')).toBe('blank');
	});

	it('accepts the limit and rejects one more character', () => {
		expect(validateDeckName('a'.repeat(DECK_NAME_MAX_LENGTH))).toBeNull();
		expect(validateDeckName('a'.repeat(DECK_NAME_MAX_LENGTH + 1))).toBe('too_long');
	});
});

describe('validateDeckDescription', () => {
	it('accepts an empty description', () => {
		expect(validateDeckDescription('')).toBeNull();
	});

	it('accepts the limit and rejects one more character', () => {
		expect(validateDeckDescription('a'.repeat(DECK_DESCRIPTION_MAX_LENGTH))).toBeNull();
		expect(validateDeckDescription('a'.repeat(DECK_DESCRIPTION_MAX_LENGTH + 1))).toBe('too_long');
	});
});

describe('validateNewDeck', () => {
	it('returns no errors for valid data', () => {
		expect(validateNewDeck({ name: 'Anatomia', description: '' })).toEqual({});
	});

	it('reports every invalid field', () => {
		expect(validateNewDeck({ name: '', description: 'a'.repeat(DECK_DESCRIPTION_MAX_LENGTH + 1) })).toEqual({
			name: 'blank',
			description: 'too_long',
		});
	});
});
