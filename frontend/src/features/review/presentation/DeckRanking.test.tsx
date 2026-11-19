import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const deckDto = {
	id: 'd1',
	name: 'Anatomia',
	description: null,
	visibility: 'PUBLIC',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: false,
	role: 'READER',
};
const ranking = [
	{ position: 1, userId: 'u2', name: 'Rita', avatar: 'STAR', reviewedFlashcards: 9, me: false },
	{ position: 1, userId: 'u3', name: 'Eduardo', avatar: 'BOOK', reviewedFlashcards: 9, me: false },
	{ position: 3, userId: 'u1', name: 'Julia', avatar: 'BOOK', reviewedFlashcards: 1, me: true },
];

describe('DeckRanking', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
		);
	});

	it('shows the ranking to participants with positions, counts and a mark on me', async () => {
		server.use(http.get('*/api/decks/d1/ranking', () => HttpResponse.json(ranking)));
		renderApp('/decks/d1');

		const list = await screen.findByRole('list', { name: 'Ranking do baralho' });
		const rows = within(list).getAllByRole('listitem');

		expect(rows).toHaveLength(3);
		expect(rows[0]).toHaveTextContent('1ºRita9 flashcards revisados');
		expect(rows[1]).toHaveTextContent('1ºEduardo');
		expect(rows[2]).toHaveTextContent('3ºJulia (você)1 flashcard revisado');
	});

	it('does not ask for the ranking when the user is not a participant', async () => {
		let asked = false;
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, role: null })),
			http.get('*/api/decks/d1/ranking', () => ((asked = true), HttpResponse.json(ranking))),
		);
		renderApp('/decks/d1');

		await screen.findByRole('heading', { name: 'Anatomia' });

		expect(screen.queryByRole('heading', { name: 'Ranking' })).not.toBeInTheDocument();
		expect(asked).toBe(false);
	});

	it('shows the ranking to the owner too and an error when it fails', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: true, role: 'OWNER' })),
			http.get('*/api/decks/d1/ranking', () => HttpResponse.json({ detail: 'x' }, { status: 500 })),
		);
		renderApp('/decks/d1');

		expect(await screen.findByText('Não foi possível carregar o ranking.')).toBeInTheDocument();
	});
});
