import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const deckDto = {
	id: 'd1',
	name: 'Anatomia',
	description: 'Ossos do corpo',
	visibility: 'PUBLIC',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: true,
	role: 'OWNER',
};

describe('DeckPage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('shows the deck name, visibility and description', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)));
		renderApp('/decks/d1');

		expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(screen.getByText('Público')).toBeInTheDocument();
		expect(screen.getByText('Ossos do corpo')).toBeInTheDocument();
	});

	it('says when the deck has no description', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, description: null })));
		renderApp('/decks/d1');

		expect(await screen.findByText('Este baralho não tem descrição.')).toBeInTheDocument();
	});

	it('shows a not found page when the deck does not exist', async () => {
		server.use(http.get('*/api/decks/missing', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));
		renderApp('/decks/missing');

		expect(await screen.findByRole('heading', { name: 'Baralho não encontrado' })).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'Voltar ao início' })).toBeInTheDocument();
	});

	it('shows an error when loading fails', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({}, { status: 500 })));
		renderApp('/decks/d1');

		expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar o baralho.');
	});

	it('lists the flashcards of the deck and invites the owner to add one', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.get('*/api/decks/d1/flashcards', () =>
				HttpResponse.json([
					{ id: 'f2', deckId: 'd1', front: 'Rádio?', back: 'Osso do antebraço', createdAt: '2026-01-02T12:00:00Z' },
					{ id: 'f1', deckId: 'd1', front: 'Úmero?', back: 'Osso do braço', createdAt: '2026-01-01T12:00:00Z' },
				]),
			),
		);
		renderApp('/decks/d1');

		expect(await screen.findByRole('heading', { name: 'Flashcards (2)' })).toBeInTheDocument();
		expect(screen.queryByText('Rádio?')).not.toBeInTheDocument();
		await userEvent.click(screen.getByRole('button', { name: 'Exibir flashcards' }));
		expect(screen.getByText('Rádio?')).toBeInTheDocument();
		expect(screen.getByText('Osso do braço')).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'Novo flashcard' })).toHaveAttribute('href', '/decks/d1/flashcards/new');
	});

	it('says when the deck has no flashcards yet', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)));
		renderApp('/decks/d1');

		expect(await screen.findByText('Este baralho ainda não tem flashcards. Crie o primeiro.')).toBeInTheDocument();
	});

	it('does not offer to add flashcards to a deck owned by someone else', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false, role: null })));
		renderApp('/decks/d1');

		expect(await screen.findByText('Este baralho ainda não tem flashcards.')).toBeInTheDocument();
		expect(screen.queryByRole('link', { name: 'Novo flashcard' })).not.toBeInTheDocument();
	});

	it('offers edit and delete to the owner', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)));
		renderApp('/decks/d1');

		expect(await screen.findByRole('link', { name: 'Editar' })).toHaveAttribute('href', '/decks/d1/edit');
		expect(screen.getByRole('button', { name: 'Excluir' })).toBeInTheDocument();
	});

	it('does not offer edit or delete to someone who does not own the deck', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false, role: null })));
		renderApp('/decks/d1');

		await screen.findByRole('heading', { name: 'Anatomia' });
		expect(screen.queryByRole('link', { name: 'Editar' })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Excluir' })).not.toBeInTheDocument();
	});

	it('asks for confirmation, then deletes the deck and goes home', async () => {
		let deleted = false;
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.delete('*/api/decks/d1', () => {
				deleted = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Excluir' }));
		const dialog = await screen.findByRole('alertdialog', { name: 'Excluir este baralho?' });
		expect(dialog).toHaveTextContent('todos os flashcards dele serão apagados');
		expect(deleted).toBe(false);
		await userEvent.click(within(dialog).getByRole('button', { name: 'Excluir baralho' }));

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
		expect(deleted).toBe(true);
	});

	it('keeps the deck when the confirmation is cancelled', async () => {
		let deleted = false;
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.delete('*/api/decks/d1', () => {
				deleted = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Excluir' }));
		await userEvent.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Cancelar' }));

		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
		expect(screen.getByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(deleted).toBe(false);
	});

	it('shows why the deletion failed and stays on the deck', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.delete('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status: 403 })),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Excluir' }));
		await userEvent.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Excluir baralho' }));

		expect(await screen.findByText('Só o dono do baralho pode excluí-lo.')).toBeInTheDocument();
		expect(screen.getByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
	});
});
