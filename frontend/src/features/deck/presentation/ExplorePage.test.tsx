import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const open = {
	id: 'p1',
	name: 'Inglês básico',
	description: 'Vocabulário do dia a dia',
	visibility: 'PUBLIC',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: false,
	role: null,
};
const joined = { ...open, id: 'p2', name: 'Verbos irregulares', role: 'READER' };

describe('ExplorePage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('is reachable from the main menu and lists the public decks of other people', async () => {
		server.use(http.get('*/api/decks/public', () => HttpResponse.json([open, joined])));
		renderApp('/');

		await userEvent.click(await screen.findByRole('link', { name: 'Explorar' }));

		expect(await screen.findByRole('heading', { name: 'Explorar' })).toBeInTheDocument();
		const list = await screen.findByRole('list', { name: 'Baralhos públicos' });
		expect(within(list).getByRole('link', { name: 'Inglês básico' })).toHaveAttribute('href', '/decks/p1');
		expect(within(list).getByRole('button', { name: 'Participar' })).toBeInTheDocument();
		expect(within(list).getByText('Você participa como Leitor')).toBeInTheDocument();
	});

	it('searches with the typed term', async () => {
		const terms: (string | null)[] = [];
		server.use(
			http.get('*/api/decks/public', ({ request }) => {
				const term = new URL(request.url).searchParams.get('search');
				terms.push(term);
				return HttpResponse.json(term ? [open] : [open, joined]);
			}),
		);
		renderApp('/explore');
		await screen.findByRole('link', { name: 'Verbos irregulares' });

		await userEvent.type(screen.getByLabelText('Pesquisar baralhos públicos'), '  inglês ');
		await userEvent.click(screen.getByRole('button', { name: 'Pesquisar' }));

		expect(await screen.findByText('Baralhos públicos com "inglês".')).toBeInTheDocument();
		expect(screen.queryByRole('link', { name: 'Verbos irregulares' })).not.toBeInTheDocument();
		expect(terms).toEqual(['', 'inglês']);
	});

	it('says when nothing was found', async () => {
		server.use(http.get('*/api/decks/public', () => HttpResponse.json([])));
		renderApp('/explore');

		expect(await screen.findByText('Ainda não há baralhos públicos de outras pessoas.')).toBeInTheDocument();

		await userEvent.type(screen.getByLabelText('Pesquisar baralhos públicos'), 'xyz');
		await userEvent.click(screen.getByRole('button', { name: 'Pesquisar' }));

		expect(await screen.findByText('Nenhum baralho público encontrado para "xyz".')).toBeInTheDocument();
	});

	it('joins a deck and then shows the role instead of the button', async () => {
		let isMember = false;
		server.use(
			http.get('*/api/decks/public', () => HttpResponse.json([{ ...open, role: isMember ? 'READER' : null }])),
			http.post('*/api/decks/p1/members', () => {
				isMember = true;
				return HttpResponse.json({ ...open, role: 'READER' }, { status: 201 });
			}),
		);
		renderApp('/explore');

		await userEvent.click(await screen.findByRole('button', { name: 'Participar' }));

		expect(await screen.findByText('Você participa como Leitor')).toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Participar' })).not.toBeInTheDocument();
	});

	it('shows a message when joining fails', async () => {
		server.use(
			http.get('*/api/decks/public', () => HttpResponse.json([open])),
			http.post('*/api/decks/p1/members', () => HttpResponse.json({ detail: 'x' }, { status: 409 })),
		);
		renderApp('/explore');

		await userEvent.click(await screen.findByRole('button', { name: 'Participar' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Você já participa deste baralho.');
	});

	it('shows a joined deck on the home page with the reader tag', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json([joined])));
		renderApp('/');

		const list = await screen.findByRole('list', { name: 'Seus baralhos' });

		expect(within(list).getByText('Leitor')).toBeInTheDocument();
	});
});
