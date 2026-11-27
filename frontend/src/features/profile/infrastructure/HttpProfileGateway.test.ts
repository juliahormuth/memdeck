import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { InvalidProfileError } from '../domain/errors';
import { HttpProfileGateway } from './HttpProfileGateway';

const profile = { id: 'u1', name: 'Julia', username: 'julia', email: 'julia@example.com', avatar: 'CAT', reviewedDays: 3, streakDays: 2, friends: 1 };
const gateway = new HttpProfileGateway();

describe('HttpProfileGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('gets the profile', async () => {
		server.use(http.get('*/api/users/me', () => HttpResponse.json(profile)));

		await expect(gateway.get()).resolves.toEqual(profile);
	});

	it('updates the profile with PUT /api/users/me', async () => {
		let body: unknown;
		server.use(
			http.put('*/api/users/me', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...profile, name: 'Julia H', avatar: 'DOG' });
			}),
		);

		const updated = await gateway.update({ name: 'Julia H', avatar: 'DOG' });

		expect(body).toEqual({ name: 'Julia H', avatar: 'DOG' });
		expect(updated.avatar).toBe('DOG');
	});

	it('translates 400', async () => {
		server.use(http.put('*/api/users/me', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));

		await expect(gateway.update({ name: 'x', avatar: 'CAT' })).rejects.toBeInstanceOf(InvalidProfileError);
	});
});
