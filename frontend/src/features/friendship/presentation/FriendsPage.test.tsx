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

	function findsBruno(relation = 'NONE') {
		server.use(
			http.get('*/api/users', () =>
				HttpResponse.json([{ userId: 'u2', name: 'Bruno', username: 'bruno', avatar: 'DOG', relation }]),
			),
		);
	}

	async function searchFor(username: string) {
		await userEvent.type(await screen.findByLabelText('Adicionar amigo'), username);
		await userEvent.click(screen.getByRole('button', { name: 'Pesquisar' }));
	}

	it('searches only when asked, shows the person and sends the request', async () => {
		const searches: string[] = [];
		let body: unknown;
		server.use(
			http.get('*/api/users', ({ request }) => {
				searches.push(new URL(request.url).searchParams.get('username') ?? '');
				return HttpResponse.json([{ userId: 'u2', name: 'Bruno', username: 'bruno', avatar: 'DOG', relation: 'NONE' }]);
			}),
			http.post('*/api/users/me/friend-requests', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);
		renderApp('/friends');

		await userEvent.type(await screen.findByLabelText('Adicionar amigo'), '  bruno');
		expect(searches).toEqual([]);
		await userEvent.click(screen.getByRole('button', { name: 'Pesquisar' }));
		await userEvent.click(await screen.findByRole('button', { name: 'Enviar pedido' }));

		expect(await screen.findByText('Pedido de amizade enviado para Bruno.')).toBeInTheDocument();
		expect(searches).toEqual(['bruno']);
		expect(body).toEqual({ username: 'bruno' });
		expect(screen.getByLabelText('Adicionar amigo')).toHaveValue('');
	});

	it('says when nobody has that username', async () => {
		server.use(http.get('*/api/users', () => HttpResponse.json([])));
		renderApp('/friends');

		await searchFor('nobody');

		expect(await screen.findByText('Nenhum usuário encontrado.')).toBeInTheDocument();
	});

	it.each([
		['SELF', 'Este é você'],
		['FRIEND', 'Vocês já são amigos'],
		['REQUEST_SENT', 'Pedido já enviado'],
		['REQUEST_RECEIVED', 'Essa pessoa já enviou um pedido para você'],
	])('does not offer a request when the relation is %s', async (relation, label) => {
		findsBruno(relation);
		renderApp('/friends');

		await searchFor('bruno');

		expect(await screen.findByText(label)).toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Enviar pedido' })).not.toBeInTheDocument();
	});

	it('tells how long to wait after too many searches', async () => {
		server.use(
			http.get('*/api/users', () =>
				HttpResponse.json({ detail: 'x' }, { status: 429, headers: { 'Retry-After': '600' } }),
			),
		);
		renderApp('/friends');

		await searchFor('bruno');

		expect(await screen.findByRole('alert')).toHaveTextContent('Tente novamente em 10 minuto(s).');
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
