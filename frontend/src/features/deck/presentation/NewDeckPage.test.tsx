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
};

describe('NewDeckPage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('asks for the deck name before sending anything', async () => {
		renderApp('/decks/new');

		await userEvent.click(await screen.findByRole('button', { name: 'Criar baralho' }));

		expect(await screen.findByText('Informe o nome do baralho.')).toBeInTheDocument();
	});

	it('rejects a description longer than the limit', async () => {
		renderApp('/decks/new');

		await userEvent.type(await screen.findByLabelText('Nome'), 'Anatomia');
		await userEvent.click(screen.getByLabelText('Descrição (opcional)'));
		await userEvent.paste('a'.repeat(501));
		await userEvent.click(screen.getByRole('button', { name: 'Criar baralho' }));

		expect(await screen.findByText('A descrição deve ter no máximo 500 caracteres.')).toBeInTheDocument();
	});

	it('creates a private deck by default and opens it', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json(deckDto, { status: 201 });
			}),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
		);
		renderApp('/decks/new');

		await userEvent.type(await screen.findByLabelText('Nome'), 'Anatomia');
		await userEvent.type(screen.getByLabelText('Descrição (opcional)'), 'Ossos do corpo');
		await userEvent.click(screen.getByRole('button', { name: 'Criar baralho' }));

		expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(screen.getByText('Privado')).toBeInTheDocument();
		expect(body).toEqual({ name: 'Anatomia', description: 'Ossos do corpo', visibility: 'PRIVATE' });
	});

	it('sends PUBLIC when the public option is chosen and omits a blank description', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...deckDto, description: null, visibility: 'PUBLIC' }, { status: 201 });
			}),
			http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, description: null, visibility: 'PUBLIC' })),
		);
		renderApp('/decks/new');

		await userEvent.type(await screen.findByLabelText('Nome'), 'Anatomia');
		await userEvent.click(screen.getByLabelText(/Público/));
		await userEvent.click(screen.getByRole('button', { name: 'Criar baralho' }));

		expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(screen.getByText('Público')).toBeInTheDocument();
		expect(body).toEqual({ name: 'Anatomia', visibility: 'PUBLIC' });
	});

	it('shows an error when the server rejects the data', async () => {
		server.use(http.post('*/api/decks', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));
		renderApp('/decks/new');

		await userEvent.type(await screen.findByLabelText('Nome'), 'Anatomia');
		await userEvent.click(screen.getByRole('button', { name: 'Criar baralho' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Os dados do baralho são inválidos.');
	});

	it('goes back to the home page when cancelled', async () => {
		renderApp('/decks/new');

		await userEvent.click(await screen.findByRole('link', { name: 'Cancelar' }));

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
	});
});
