import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../test/renderApp';
import { server } from '../../test/server';

describe('MainLayout pending counters', () => {
	let invitationRequests: number;
	let friendRequestRequests: number;
	let pendingInvitations: unknown[];

	beforeEach(() => {
		invitationRequests = 0;
		friendRequestRequests = 0;
		pendingInvitations = [];
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/invitations', () => {
				invitationRequests += 1;
				return HttpResponse.json(pendingInvitations);
			}),
			http.get('*/api/users/me/friend-requests', () => {
				friendRequestRequests += 1;
				return HttpResponse.json([]);
			}),
			http.get('*/api/decks/public', () => HttpResponse.json([])),
		);
	});

	it('loads the counters once when the layout opens', async () => {
		renderApp('/');

		await screen.findByRole('link', { name: 'Explorar' });
		await waitFor(() => expect(invitationRequests).toBe(1));

		expect(friendRequestRequests).toBe(1);
	});

	it('asks again every time a menu item is clicked, including the page the user is already on', async () => {
		renderApp('/');
		await waitFor(() => expect(invitationRequests).toBe(1));

		await userEvent.click(screen.getByRole('link', { name: 'Explorar' }));
		await waitFor(() => expect(invitationRequests).toBe(2));
		expect(friendRequestRequests).toBe(2);

		await userEvent.click(screen.getByRole('link', { name: 'Explorar' }));
		await waitFor(() => expect(invitationRequests).toBe(3));
		expect(friendRequestRequests).toBe(3);

		await userEvent.click(screen.getByRole('link', { name: 'Início' }));
		await waitFor(() => expect(invitationRequests).toBe(4));
	});

	it('shows a new invitation in the badge after the user navigates', async () => {
		renderApp('/');
		await waitFor(() => expect(invitationRequests).toBe(1));
		expect(screen.queryByLabelText('1 pendentes')).not.toBeInTheDocument();

		pendingInvitations = [
			{ id: 'i1', deckId: 'd1', deckName: 'Anatomia', inviterName: 'Olivia', role: 'READER', createdAt: '2026-01-01T12:00:00Z' },
		];
		await userEvent.click(screen.getByRole('link', { name: 'Explorar' }));

		expect(await screen.findByLabelText('1 pendentes')).toBeInTheDocument();
	});
});

describe('MainLayout profile link', () => {
	it('shows the username, not the e-mail, in the menu', async () => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
		renderApp('/');

		const link = await screen.findByRole('link', { name: 'Meu perfil' });

		await waitFor(() => expect(link).toHaveTextContent('@julia'));
		expect(screen.queryByText('julia@example.com')).not.toBeInTheDocument();
	});
});
