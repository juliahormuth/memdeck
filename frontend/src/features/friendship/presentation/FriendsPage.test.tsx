import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const incoming = { id: 'r1', direction: 'INCOMING', userId: 'u2', name: 'Bruno', avatar: 'DOG', createdAt: '2026-01-02T12:00:00Z' };
const outgoing = { id: 'r2', direction: 'OUTGOING', userId: 'u3', name: 'Carla', avatar: 'CAT', createdAt: '2026-01-03T12:00:00Z' };
const friend = { userId: 'u4', name: 'Diego', avatar: 'RABBIT', since: '2026-01-01T12:00:00Z' };

describe('FriendsPage', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/users/me/friends', () => HttpResponse.json([])),
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json([])),
		);
	});

	it('shows a count of received requests in the menu and opens the page', async () => {
		server.use(http.get('*/api/users/me/friend-requests', () => HttpResponse.json([incoming, outgoing])));
		renderApp('/');

		const link = await screen.findByRole('link', { name: /Amigos/ });
		expect(await within(link).findByLabelText('1 pendentes')).toBeInTheDocument();
		await userEvent.click(link);

		expect(await screen.findByRole('heading', { name: 'Amigos' })).toBeInTheDocument();
	});

	it('lists received requests, sent requests and friends in separate sections', async () => {
		server.use(
			http.get('*/api/users/me/friends', () => HttpResponse.json([friend])),
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json([incoming, outgoing])),
		);
		renderApp('/friends');

		expect(within(await screen.findByRole('list', { name: 'Pedidos recebidos' })).getByText('Bruno')).toBeInTheDocument();
		expect(within(screen.getByRole('list', { name: 'Pedidos enviados' })).getByText('Carla')).toBeInTheDocument();
		expect(within(await screen.findByRole('list', { name: 'Seus amigos' })).getByText('Diego')).toBeInTheDocument();
		expect(screen.getByRole('heading', { name: 'Seus amigos (1)' })).toBeInTheDocument();
	});

	it('says when there is nothing in a section', async () => {
		renderApp('/friends');

		expect(await screen.findByText('Nenhum pedido recebido.')).toBeInTheDocument();
		expect(screen.getByText('Nenhum pedido pendente.')).toBeInTheDocument();
		expect(await screen.findByText('Você ainda não tem amigos adicionados.')).toBeInTheDocument();
	});

	it('sends a request by e-mail and answers the same whether or not the account exists', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/users/me/friend-requests', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);
		renderApp('/friends');

		await userEvent.type(await screen.findByLabelText('Adicionar amigo por e-mail'), '  bruno@example.com');
		await userEvent.click(screen.getByRole('button', { name: 'Enviar pedido' }));

		expect(await screen.findByText(/Se esse e-mail tiver uma conta/)).toBeInTheDocument();
		expect(body).toEqual({ email: 'bruno@example.com' });
		expect(screen.getByLabelText('Adicionar amigo por e-mail')).toHaveValue('');
	});

	it('shows an error when the e-mail is refused', async () => {
		server.use(http.post('*/api/users/me/friend-requests', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));
		renderApp('/friends');

		await userEvent.type(await screen.findByLabelText('Adicionar amigo por e-mail'), 'julia@example.com');
		await userEvent.click(screen.getByRole('button', { name: 'Enviar pedido' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Confira o e-mail');
	});

	it('accepts a request and the person moves to the friends list', async () => {
		let accepted = false;
		server.use(
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json(accepted ? [] : [incoming])),
			http.get('*/api/users/me/friends', () =>
				HttpResponse.json(accepted ? [{ userId: 'u2', name: 'Bruno', avatar: 'DOG', since: '2026-01-04T12:00:00Z' }] : []),
			),
			http.post('*/api/users/me/friend-requests/r1/accept', () => {
				accepted = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/friends');

		await userEvent.click(await screen.findByRole('button', { name: 'Aceitar pedido de Bruno' }));

		expect(within(await screen.findByRole('list', { name: 'Seus amigos' })).getByText('Bruno')).toBeInTheDocument();
		expect(screen.getByText('Nenhum pedido recebido.')).toBeInTheDocument();
	});

	it('declines a received request and cancels a sent one', async () => {
		let requests = [incoming, outgoing];
		server.use(
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json(requests)),
			http.post('*/api/users/me/friend-requests/r1/decline', () => {
				requests = requests.filter((request) => request.id !== 'r1');
				return new HttpResponse(null, { status: 204 });
			}),
			http.delete('*/api/users/me/friend-requests/r2', () => {
				requests = requests.filter((request) => request.id !== 'r2');
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/friends');

		await userEvent.click(await screen.findByRole('button', { name: 'Recusar pedido de Bruno' }));
		await screen.findByText('Nenhum pedido recebido.');
		await userEvent.click(screen.getByRole('button', { name: 'Cancelar pedido para Carla' }));

		expect(await screen.findByText('Nenhum pedido pendente.')).toBeInTheDocument();
	});

	it('removes a friend after confirmation', async () => {
		let friends = [friend];
		server.use(
			http.get('*/api/users/me/friends', () => HttpResponse.json(friends)),
			http.delete('*/api/users/me/friends/u4', () => {
				friends = [];
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/friends');

		await userEvent.click(await screen.findByRole('button', { name: 'Remover Diego dos amigos' }));
		const dialog = await screen.findByRole('alertdialog', { name: 'Remover amigo?' });
		await userEvent.click(within(dialog).getByRole('button', { name: 'Remover' }));

		expect(await screen.findByText('Você ainda não tem amigos adicionados.')).toBeInTheDocument();
	});

	it('shows a message when answering a request that no longer exists', async () => {
		server.use(
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json([incoming])),
			http.post('*/api/users/me/friend-requests/r1/accept', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);
		renderApp('/friends');

		await userEvent.click(await screen.findByRole('button', { name: 'Aceitar pedido de Bruno' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Esse pedido não existe mais.');
	});
});
