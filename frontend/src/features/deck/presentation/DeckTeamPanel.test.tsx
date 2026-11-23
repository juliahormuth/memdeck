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

	it('opens a window to invite by e-mail with the chosen role, closes it, and answers the same whether or not the account exists', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/invitations', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await userEvent.type(within(dialog).getByLabelText('E-mail da pessoa'), '  rita@example.com');
		await userEvent.selectOptions(within(dialog).getByLabelText('Papel'), 'EDITOR');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Convidar' }));

		expect(await screen.findByText(/Se esse e-mail tiver uma conta/)).toBeInTheDocument();
		expect(body).toEqual({ email: 'rita@example.com', role: 'EDITOR' });
		await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
	});

	it('starts the next invitation with an empty e-mail and the reader role', async () => {
		server.use(http.post('*/api/decks/d1/invitations', () => new HttpResponse(null, { status: 202 })));
		renderApp('/decks/d1');
		let dialog = await openAddDialog();
		await userEvent.type(within(dialog).getByLabelText('E-mail da pessoa'), 'rita@example.com');
		await userEvent.selectOptions(within(dialog).getByLabelText('Papel'), 'EDITOR');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Convidar' }));
		await screen.findByText(/Se esse e-mail tiver uma conta/);

		dialog = await openAddDialog();

		expect(within(dialog).getByLabelText('E-mail da pessoa')).toHaveValue('');
		expect(within(dialog).getByLabelText('Papel')).toHaveValue('READER');
		expect(screen.queryByText(/Se esse e-mail tiver uma conta/)).not.toBeInTheDocument();
	});

	it('closes the window with Cancelar without sending anything', async () => {
		let called = false;
		server.use(http.post('*/api/decks/d1/invitations', () => ((called = true), new HttpResponse(null, { status: 202 }))));
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await userEvent.type(within(dialog).getByLabelText('E-mail da pessoa'), 'rita@example.com');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Cancelar' }));

		await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
		expect(called).toBe(false);
	});

	it('keeps the window open and shows an error when the e-mail is refused', async () => {
		server.use(http.post('*/api/decks/d1/invitations', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));
		renderApp('/decks/d1');

		const dialog = await openAddDialog();
		await userEvent.type(within(dialog).getByLabelText('E-mail da pessoa'), 'julia@example.com');
		await userEvent.click(within(dialog).getByRole('button', { name: 'Convidar' }));

		expect(await within(dialog).findByRole('alert')).toHaveTextContent('Confira o e-mail');
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
