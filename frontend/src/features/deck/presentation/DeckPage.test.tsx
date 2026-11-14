import { screen } from '@testing-library/react';
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
});
