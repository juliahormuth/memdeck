import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const deckDto = {
	id: 'd1',
	name: 'Anatomia',
	description: 'Ossos do corpo',
	visibility: 'PRIVATE',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: true,
};

describe('EditDeckPage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('starts with the current data of the deck', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)));
		renderApp('/decks/d1/edit');

		expect(await screen.findByLabelText('Nome')).toHaveValue('Anatomia');
		expect(screen.getByLabelText('Descrição (opcional)')).toHaveValue('Ossos do corpo');
		expect(screen.getByLabelText(/Privado/)).toBeChecked();
	});

	it('saves the changes and goes back to the deck page', async () => {
		let body: unknown;
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.put('*/api/decks/d1', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...deckDto, name: 'Anatomia humana', visibility: 'PUBLIC' });
			}),
		);
		renderApp('/decks/d1/edit');

		const name = await screen.findByLabelText('Nome');
		await userEvent.clear(name);
		await userEvent.type(name, 'Anatomia humana');
		await userEvent.click(screen.getByLabelText(/Público/));
		await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByRole('heading', { name: 'Anatomia humana' })).toBeInTheDocument();
		expect(body).toEqual({ name: 'Anatomia humana', description: 'Ossos do corpo', visibility: 'PUBLIC' });
	});

	it('does not send a blank name', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)));
		renderApp('/decks/d1/edit');

		await userEvent.clear(await screen.findByLabelText('Nome'));
		await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByText('Informe o nome do baralho.')).toBeInTheDocument();
	});

	it('explains when the server refuses the change', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.put('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status: 403 })),
		);
		renderApp('/decks/d1/edit');

		await userEvent.click(await screen.findByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Só o dono do baralho pode editá-lo.');
	});

	it('does not offer the form to someone who does not own the deck', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false })));
		renderApp('/decks/d1/edit');

		expect(await screen.findByText('Só o dono do baralho pode editá-lo.')).toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Salvar alterações' })).not.toBeInTheDocument();
	});

	it('shows not found for a deck that does not exist', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));
		renderApp('/decks/d1/edit');

		expect(await screen.findByRole('heading', { name: 'Baralho não encontrado' })).toBeInTheDocument();
	});
});
