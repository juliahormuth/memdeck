import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const deckDto = {
	id: 'd1',
	name: 'Anatomia',
	description: null,
	visibility: 'PRIVATE',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: true,
};
const first = { id: 'f1', deckId: 'd1', front: 'Úmero?', back: 'Osso do braço', createdAt: '2026-01-01T12:00:00Z' };
const second = { id: 'f2', deckId: 'd1', front: 'Rádio?', back: 'Antebraço', createdAt: '2026-01-02T12:00:00Z' };

describe('FlashcardList', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
		);
	});

	it('lets the owner edit each flashcard', async () => {
		server.use(http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([second, first])));
		renderApp('/decks/d1');

		const links = await screen.findAllByRole('link', { name: 'Editar flashcard' });

		expect(links.map((link) => link.getAttribute('href'))).toEqual([
			'/decks/d1/flashcards/f2/edit',
			'/decks/d1/flashcards/f1/edit',
		]);
	});

	it('shows no edit or delete actions to someone who does not own the deck', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false })),
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([first])),
		);
		renderApp('/decks/d1');

		await screen.findByText('Úmero?');

		expect(screen.queryByRole('link', { name: 'Editar flashcard' })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Excluir flashcard' })).not.toBeInTheDocument();
	});

	it('asks for confirmation and then removes the flashcard from the list', async () => {
		let remaining = [second, first];
		server.use(
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json(remaining)),
			http.delete('*/api/decks/d1/flashcards/f2', () => {
				remaining = [first];
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		await userEvent.click((await screen.findAllByRole('button', { name: 'Excluir flashcard' }))[0]!);
		const dialog = await screen.findByRole('alertdialog', { name: 'Excluir este flashcard?' });
		expect(dialog).toHaveTextContent('"Rádio?" será apagado');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Excluir flashcard' }));

		expect(await screen.findByRole('heading', { name: 'Flashcards (1)' })).toBeInTheDocument();
		expect(screen.queryByText('Rádio?')).not.toBeInTheDocument();
		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('keeps the flashcard when the confirmation is cancelled', async () => {
		let deleted = false;
		server.use(
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([first])),
			http.delete('*/api/decks/d1/flashcards/f1', () => {
				deleted = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Excluir flashcard' }));
		await userEvent.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Cancelar' }));

		expect(screen.getByText('Úmero?')).toBeInTheDocument();
		expect(deleted).toBe(false);
	});

	it('shows why the deletion failed', async () => {
		server.use(
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([first])),
			http.delete('*/api/decks/d1/flashcards/f1', () => HttpResponse.json({ detail: 'x' }, { status: 403 })),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Excluir flashcard' }));
		await userEvent.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Excluir flashcard' }));

		expect(await screen.findByText('Só o dono do baralho pode excluir flashcards.')).toBeInTheDocument();
	});
});
