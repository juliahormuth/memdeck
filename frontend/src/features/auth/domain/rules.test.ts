import { describe, expect, it } from 'vitest';
import {
	EMAIL_MAX_LENGTH,
	NAME_MAX_LENGTH,
	PASSWORD_MAX_LENGTH,
	PASSWORD_MIN_LENGTH,
	validateEmail,
	validateName,
	validatePassword,
	validateRegistration,
} from './rules';

describe('validateName', () => {
	it('accepts a normal name and trims blanks', () => {
		expect(validateName('  Julia  ')).toBeNull();
	});

	it('rejects a blank name', () => {
		expect(validateName('   ')).toBe('blank');
	});

	it('accepts the limit and rejects one more character', () => {
		expect(validateName('a'.repeat(NAME_MAX_LENGTH))).toBeNull();
		expect(validateName('a'.repeat(NAME_MAX_LENGTH + 1))).toBe('too_long');
	});
});

describe('validateEmail', () => {
	it('accepts a valid email', () => {
		expect(validateEmail('julia@example.com')).toBeNull();
	});

	it('rejects a blank email', () => {
		expect(validateEmail('  ')).toBe('blank');
	});

	it.each(['no-at-sign', 'a@b', 'a b@c.com', 'a@@b.com'])('rejects the malformed email %s', (email) => {
		expect(validateEmail(email)).toBe('invalid');
	});

	it('rejects an email longer than the limit', () => {
		expect(validateEmail(`${'a'.repeat(EMAIL_MAX_LENGTH)}@b.com`)).toBe('invalid');
	});
});

describe('validatePassword', () => {
	it('accepts the minimum and the maximum length', () => {
		expect(validatePassword('a'.repeat(PASSWORD_MIN_LENGTH))).toBeNull();
		expect(validatePassword('a'.repeat(PASSWORD_MAX_LENGTH))).toBeNull();
	});

	it('rejects a short password', () => {
		expect(validatePassword('a'.repeat(PASSWORD_MIN_LENGTH - 1))).toBe('too_short');
	});

	it('rejects a password longer than the limit', () => {
		expect(validatePassword('a'.repeat(PASSWORD_MAX_LENGTH + 1))).toBe('too_long');
	});
});

describe('validateRegistration', () => {
	it('returns no errors for a valid registration', () => {
		expect(validateRegistration({ name: 'Julia', email: 'julia@example.com', password: 'correct-horse' })).toEqual({});
	});

	it('reports every invalid field', () => {
		expect(validateRegistration({ name: '', email: 'nope', password: 'short' })).toEqual({
			name: 'blank',
			email: 'invalid',
			password: 'too_short',
		});
	});
});
