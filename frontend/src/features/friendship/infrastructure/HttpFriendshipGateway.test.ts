import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { FriendshipNotFoundError, InvalidFriendRequestError } from '../domain/errors';
import { HttpFriendshipGateway } from './HttpFriendshipGateway';

const gateway = new HttpFriendshipGateway();
const noContent = () => new HttpResponse(null, { status: 204 });

describe('HttpFriendshipGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('lists friends and requests', async () => {
		const friend = { userId: 'u2', name: 'Bruno', avatar: 'DOG', since: '2026-01-01T12:00:00Z' };
		const request = { id: 'r1', direction: 'INCOMING', userId: 'u3', name: 'Carla', avatar: 'CAT', createdAt: '2026-01-02T12:00:00Z' };
		server.use(
			http.get('*/api/users/me/friends', () => HttpResponse.json([friend])),
			http.get('*/api/users/me/friend-requests', () => HttpResponse.json([request])),
		);

		await expect(gateway.listFriends()).resolves.toEqual([friend]);
		await expect(gateway.listRequests()).resolves.toEqual([request]);
	});

	it('finds a person by the exact username, or nobody', async () => {
		const found = { userId: 'u2', name: 'Bruno', username: 'bruno', avatar: 'DOG', relation: 'NONE' };
		server.use(
			http.get('*/api/users', ({ request }) =>
				HttpResponse.json(new URL(request.url).searchParams.get('username') === 'bruno' ? [found] : []),
			),
		);

		await expect(gateway.searchUser('bruno')).resolves.toEqual(found);
		await expect(gateway.searchUser('a&b')).resolves.toBeNull();
	});

	it('translates 429 into TooManySearchesError with the waiting time', async () => {
		server.use(
			http.get('*/api/users', () => HttpResponse.json({}, { status: 429, headers: { 'Retry-After': '60' } })),
		);

		await expect(gateway.searchUser('bruno')).rejects.toMatchObject({ name: 'TooManySearchesError', retryAfterSeconds: 60 });
	});

	it('sends a request with the username in the body', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/users/me/friend-requests', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);

		await gateway.sendRequest('bruno');

		expect(body).toEqual({ username: 'bruno' });
	});

	it('answers, cancels and removes with the right routes', async () => {
		const calls: string[] = [];
		server.use(
			http.post('*/api/users/me/friend-requests/r1/accept', () => (calls.push('accept'), noContent())),
			http.post('*/api/users/me/friend-requests/r1/decline', () => (calls.push('decline'), noContent())),
			http.delete('*/api/users/me/friend-requests/r1', () => (calls.push('cancel'), noContent())),
			http.delete('*/api/users/me/friends/u2', () => (calls.push('remove'), noContent())),
		);

		await gateway.accept('r1');
		await gateway.decline('r1');
		await gateway.cancel('r1');
		await gateway.removeFriend('u2');

		expect(calls).toEqual(['accept', 'decline', 'cancel', 'remove']);
	});

	it('translates 400 when sending and 404 when answering', async () => {
		server.use(
			http.post('*/api/users/me/friend-requests', () => HttpResponse.json({ detail: 'x' }, { status: 400 })),
			http.post('*/api/users/me/friend-requests/r1/accept', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);

		await expect(gateway.sendRequest('x')).rejects.toBeInstanceOf(InvalidFriendRequestError);
		await expect(gateway.accept('r1')).rejects.toBeInstanceOf(FriendshipNotFoundError);
	});
});
