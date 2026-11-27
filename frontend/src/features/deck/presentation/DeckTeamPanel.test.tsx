import { screen, waitFor, within } from '@testing-library/react';
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
	role: 'OWNER',
};
const rita = { userId: 'u2', name: 'Rita', role: 'READER', joinedAt: '2026-01-02T12:00:00Z' };

describe('DeckTeamPanel', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.get('*/api/decks/d1/members', () => HttpResponse.json([])),
			http.get('*/api/decks/d1/invitations', () => HttpResponse.json([])),
		);
	});

	it('is shown only to the owner', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false, role: 'EDITOR' })));
		renderApp('/decks/d1');

		await screen.findByRole('heading', { name: 'Anatomia' });

		expect(screen.queryByRole('heading', { name: 'Participantes' })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Adicionar participante' })).not.toBeInTheDocument();
	});

	async function openAddDialog() {
		await userEvent.click(await screen.findByRole('button', { name: 'Adicionar participante' }));
		return screen.findByRole('dialog', { name: 'Adicionar participante' });
	}

	it('shows only the participants until the add button is used', async () => {
		server.use(http.get('*/api/decks/d1/members', () => HttpResponse.json([rita])));
		renderApp('/decks/d1');

		await screen.findByRole('list', { name: 'Participantes' });

		expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
		expect(screen.queryByRole('list', { name: 'Convites pendentes' })).not.toBeInTheDocument();
		expect(screen.queryByText('Convites pendentes')).not.toBeInTheDocument();
	});

	function findsRita(relation = 'NONE') {
		server.use(
			http.get('*/api/users', () =>
				HttpResponse.json([{ userId: 'u2', name: 'Rita', username: 'rita', avatar: 'CAT', relation }]),
			),
		);
	}

	async function searchFor(dialog: HTMLElement, username: string) {
		await userEvent.type(within(dialog).getByLabelText('Nome de usuário'), username);
		await userEvent.click(within(dialog).getByRole('button', { name: 'Pesquisar' }));
	}

	it('finds the person by username and invites with the chosen role', async () => {
		findsRita();
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/invitations', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await userEvent.selectOptions(within(dialog).getByLabelText('Papel'), 'EDITOR');
		await searchFor(dialog, 'rita');
		expect(await within(dialog).findByText('@rita')).toBeInTheDocument();
		await userEvent.click(within(dialog).getByRole('button', { name: 'Convidar' }));

		expect(await screen.findByText('Convite enviado para Rita.')).toBeInTheDocument();
		expect(body).toEqual({ username: 'rita', role: 'EDITOR' });
		await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
	});

	it('says when nobody has that username', async () => {
		server.use(http.get('*/api/users', () => HttpResponse.json([])));
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await searchFor(dialog, 'nobody');

		expect(await within(dialog).findByText('Nenhum usuário encontrado.')).toBeInTheDocument();
		expect(within(dialog).queryByRole('button', { name: 'Convidar' })).not.toBeInTheDocument();
	});

	it('does not offer to invite the owner or someone who already has the role', async () => {
		findsRita();
		server.use(http.get('*/api/decks/d1/members', () => HttpResponse.json([rita])));
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await searchFor(dialog, 'rita');

		expect(await within(dialog).findByText('Já participa como Leitor')).toBeInTheDocument();
		await userEvent.selectOptions(within(dialog).getByLabelText('Papel'), 'EDITOR');
		expect(within(dialog).getByRole('button', { name: 'Convidar' })).toBeInTheDocument();
	});

	it('starts the next invitation with an empty search and the reader role', async () => {
		findsRita();
		renderApp('/decks/d1');
		let dialog = await openAddDialog();
		await userEvent.selectOptions(within(dialog).getByLabelText('Papel'), 'EDITOR');
		await searchFor(dialog, 'rita');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Cancelar' }));
		await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());

		dialog = await openAddDialog();

		expect(within(dialog).getByLabelText('Nome de usuário')).toHaveValue('');
		expect(within(dialog).getByLabelText('Papel')).toHaveValue('READER');
	});

	it('keeps the window open and shows an error when the invitation is refused', async () => {
		findsRita();
		server.use(http.post('*/api/decks/d1/invitations', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await searchFor(dialog, 'rita');
		await userEvent.click(await within(dialog).findByRole('button', { name: 'Convidar' }));

		expect(await within(dialog).findByRole('alert')).toHaveTextContent('Confira o nome de usuário');
		expect(screen.getByRole('dialog', { name: 'Adicionar participante' })).toBeInTheDocument();
	});

	it('lists the pending invitations only when there are some, and cancels one', async () => {
		let pending = [{ id: 'i1', inviteeName: 'Rita', role: 'READER', createdAt: '2026-01-02T12:00:00Z' }];
		server.use(
			http.get('*/api/decks/d1/invitations', () => HttpResponse.json(pending)),
			http.delete('*/api/decks/d1/invitations/i1', () => {
				pending = [];
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		const list = await screen.findByRole('list', { name: 'Convites pendentes' });
		expect(within(list).getByText('Rita')).toBeInTheDocument();
		await userEvent.click(within(list).getByRole('button', { name: 'Cancelar convite de Rita' }));

		await waitFor(() => expect(screen.queryByRole('list', { name: 'Convites pendentes' })).not.toBeInTheDocument());
		expect(screen.queryByText('Convites pendentes')).not.toBeInTheDocument();
	});

	it('lists the participants and removes one after confirmation', async () => {
		let members = [rita];
		server.use(
			http.get('*/api/decks/d1/members', () => HttpResponse.json(members)),
			http.delete('*/api/decks/d1/members/u2', () => {
				members = [];
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/decks/d1');

		const list = await screen.findByRole('list', { name: 'Participantes' });
		expect(screen.getByRole('heading', { name: 'Participantes (1)' })).toBeInTheDocument();
		expect(within(list).getByText('Leitor')).toBeInTheDocument();
		await userEvent.click(within(list).getByRole('button', { name: 'Remover Rita' }));
		const dialog = await screen.findByRole('alertdialog', { name: 'Remover participante?' });
		await userEvent.click(within(dialog).getByRole('button', { name: 'Remover' }));

		expect(await screen.findByText('Apenas você')).toBeInTheDocument();
		expect(screen.getByRole('heading', { name: 'Participantes' })).toBeInTheDocument();
	});

	it('shows a message when removing fails', async () => {
		server.use(
			http.get('*/api/decks/d1/members', () => HttpResponse.json([rita])),
			http.delete('*/api/decks/d1/members/u2', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Remover Rita' }));
		await userEvent.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Remover' }));

		expect(await screen.findByText('Essa pessoa já não participa do baralho.')).toBeInTheDocument();
	});
});
