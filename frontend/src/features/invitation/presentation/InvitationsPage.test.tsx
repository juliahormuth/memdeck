import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const editorInvitation = {
	id: 'i1',
	deckId: 'd1',
	deckName: 'Anatomia',
	inviterName: 'Olivia',
	role: 'EDITOR',
	createdAt: '2026-01-01T12:00:00Z',
};
const readerInvitation = { ...editorInvitation, id: 'i2', deckId: 'd2', deckName: 'Inglês', role: 'READER' };

describe('InvitationsPage', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('shows a count of pending invitations in the menu and lists them with the role explained', async () => {
		server.use(http.get('*/api/invitations', () => HttpResponse.json([editorInvitation, readerInvitation])));
		renderApp('/');

		const menuLink = await screen.findByRole('link', { name: /Convites/ });
		expect(await within(menuLink).findByLabelText('2 pendentes')).toBeInTheDocument();
		await userEvent.click(menuLink);

		const list = await screen.findByRole('list', { name: 'Convites pendentes' });
		expect(within(list).getAllByRole('listitem')).toHaveLength(2);
		expect(within(list).getAllByText(/Olivia/)).toHaveLength(2);
		expect(within(list).getByText('Pode estudar e também criar, editar e excluir flashcards.')).toBeInTheDocument();
		expect(within(list).getByText('Pode estudar o baralho e ver seus flashcards.')).toBeInTheDocument();
	});

	it('shows no count when there is nothing pending and says so on the page', async () => {
		renderApp('/invitations');

		expect(await screen.findByText('Você não tem convites pendentes.')).toBeInTheDocument();
		expect(within(screen.getByRole('link', { name: 'Convites' })).queryByLabelText(/pendentes/)).not.toBeInTheDocument();
	});

	it('accepts an invitation and opens the deck', async () => {
		let pending = [editorInvitation];
		server.use(
			http.get('*/api/invitations', () => HttpResponse.json(pending)),
			http.post('*/api/invitations/i1/accept', () => {
				pending = [];
				return HttpResponse.json({ deckId: 'd1', role: 'EDITOR' });
			}),
			http.get('*/api/decks/d1', () =>
				HttpResponse.json({ id: 'd1', name: 'Anatomia', description: null, visibility: 'PRIVATE', createdAt: '2026-01-01T12:00:00Z', ownedByMe: false, role: 'EDITOR' }),
			),
		);
		const { router } = renderApp('/invitations');

		await userEvent.click(await screen.findByRole('button', { name: 'Aceitar' }));

		expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(router.state.location.pathname).toBe('/decks/d1');
		expect(screen.getByText('Você participa como Editor')).toBeInTheDocument();
	});

	it('declines an invitation and removes it from the list', async () => {
		let pending = [editorInvitation];
		server.use(
			http.get('*/api/invitations', () => HttpResponse.json(pending)),
			http.post('*/api/invitations/i1/decline', () => {
				pending = [];
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/invitations');

		await userEvent.click(await screen.findByRole('button', { name: 'Recusar' }));

		expect(await screen.findByText('Você não tem convites pendentes.')).toBeInTheDocument();
	});

	it('shows a message when the invitation is gone', async () => {
		server.use(
			http.get('*/api/invitations', () => HttpResponse.json([editorInvitation])),
			http.post('*/api/invitations/i1/accept', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);
		renderApp('/invitations');

		await userEvent.click(await screen.findByRole('button', { name: 'Aceitar' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Esse convite não existe mais.');
	});
});
