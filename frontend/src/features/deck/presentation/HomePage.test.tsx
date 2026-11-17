import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const english = { id: 'd2', name: 'English', description: 'Daily words', visibility: 'PUBLIC', createdAt: '2026-01-02T10:00:00Z' };
const anatomy = { id: 'd1', name: 'Anatomia', description: null, visibility: 'PRIVATE', createdAt: '2026-01-01T10:00:00Z' };

describe('HomePage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('lists my decks in the order received, with visibility and a link to each one', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json([english, anatomy])));
		renderApp('/');

		const list = await screen.findByRole('list', { name: 'Seus baralhos' });
		const names = within(list).getAllByRole('heading', { level: 2 }).map((heading) => heading.textContent);
		expect(names).toEqual(['English', 'Anatomia']);
		expect(within(list).getByText('Público')).toBeInTheDocument();
		expect(within(list).getByText('Privado')).toBeInTheDocument();
		expect(within(list).getByText('Daily words')).toBeInTheDocument();
		expect(within(list).getByText('Sem descrição')).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'English' })).toHaveAttribute('href', '/decks/d2');
	});

	it('shows how many decks there are', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json([english, anatomy])));
		renderApp('/');

		expect(await screen.findByText('2 baralhos')).toBeInTheDocument();
	});

	it('uses the singular for a single deck', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json([english])));
		renderApp('/');

		expect(await screen.findByText('1 baralho')).toBeInTheDocument();
	});

	it('invites the user to create the first deck when there are none', async () => {
		renderApp('/');

		expect(await screen.findByRole('heading', { name: 'Seus baralhos aparecerão aqui' })).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'Criar baralho' })).toHaveAttribute('href', '/decks/new');
		expect(screen.queryByRole('list', { name: 'Seus baralhos' })).not.toBeInTheDocument();
	});

	it('says it is loading before the decks arrive', async () => {
		renderApp('/');

		expect(await screen.findByText('Carregando seus baralhos...')).toBeInTheDocument();
		expect(await screen.findByRole('heading', { name: 'Seus baralhos aparecerão aqui' })).toBeInTheDocument();
	});

	it('shows an error and lets the user try again', async () => {
		let calls = 0;
		server.use(
			http.get('*/api/decks', () => {
				calls += 1;
				return calls === 1 ? HttpResponse.json({}, { status: 500 }) : HttpResponse.json([english]);
			}),
		);
		renderApp('/');

		expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar seus baralhos.');
		await userEvent.click(screen.getByRole('button', { name: 'Tentar novamente' }));

		expect(await screen.findByRole('link', { name: 'English' })).toBeInTheDocument();
		expect(screen.queryByRole('alert')).not.toBeInTheDocument();
	});

	it('opens a deck when its card is clicked', async () => {
		server.use(
			http.get('*/api/decks', () => HttpResponse.json([english])),
			http.get('*/api/decks/d2', () => HttpResponse.json(english)),
		);
		renderApp('/');

		await userEvent.click(await screen.findByRole('link', { name: 'English' }));

		expect(await screen.findByRole('heading', { level: 1, name: 'English' })).toBeInTheDocument();
		expect(screen.getByText('Daily words')).toBeInTheDocument();
	});
});
