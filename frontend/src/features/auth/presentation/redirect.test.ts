import { describe, expect, it } from 'vitest';
import { loginPathFor, safeRedirectPath } from './redirect';

describe('safeRedirectPath', () => {
	it.each(['/', '/decks/new', '/decks/123?tab=cards', '/decks/123#top'])('accepts the internal path %s', (path) => {
		expect(safeRedirectPath(path)).toBe(path);
	});

	it.each([null, '', 'decks', 'https://evil.com', '//evil.com', '/\\evil.com', 'javascript:alert(1)'])(
		'rejects %s',
		(value) => {
			expect(safeRedirectPath(value)).toBeNull();
		},
	);

	it.each(['/login', '/login?redirect=/', '/register'])('rejects the authentication page %s to avoid a loop', (path) => {
		expect(safeRedirectPath(path)).toBeNull();
	});
});

describe('loginPathFor', () => {
	it('goes straight to the login page from the home page', () => {
		expect(loginPathFor({ pathname: '/', search: '' })).toBe('/login');
	});

	it('remembers where the user was, including the query string', () => {
		expect(loginPathFor({ pathname: '/decks/new', search: '' })).toBe('/login?redirect=%2Fdecks%2Fnew');
		expect(loginPathFor({ pathname: '/decks/1', search: '?tab=cards' })).toBe('/login?redirect=%2Fdecks%2F1%3Ftab%3Dcards');
	});
});
