import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import {
	EmailAlreadyRegisteredError,
	InvalidCredentialsError,
	InvalidRegistrationError,
	TooManyLoginAttemptsError,
} from '../domain/errors';
import { HttpAuthGateway } from './HttpAuthGateway';

const gateway = new HttpAuthGateway();

describe('HttpAuthGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('registers an account with POST /api/auth/register', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/auth/register', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ id: 'u1', name: 'Julia', email: 'julia@example.com' }, { status: 201 });
			}),
		);

		await gateway.register({ name: 'Julia', email: 'julia@example.com', password: 'correct-horse' });

		expect(body).toEqual({ name: 'Julia', email: 'julia@example.com', password: 'correct-horse' });
	});

	it('translates 409 into EmailAlreadyRegisteredError', async () => {
		server.use(http.post('*/api/auth/register', () => HttpResponse.json({ detail: 'x' }, { status: 409 })));

		await expect(gateway.register({ name: 'J', email: 'j@example.com', password: 'correct-horse' })).rejects.toBeInstanceOf(
			EmailAlreadyRegisteredError,
		);
	});

	it('translates 400 on register into InvalidRegistrationError', async () => {
		server.use(http.post('*/api/auth/register', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));

		await expect(gateway.register({ name: 'J', email: 'j@example.com', password: 'correct-horse' })).rejects.toBeInstanceOf(
			InvalidRegistrationError,
		);
	});

	it('maps the login response to a User', async () => {
		server.use(http.post('*/api/auth/login', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));

		await expect(gateway.login({ email: 'julia@example.com', password: 'correct-horse' })).resolves.toEqual({
			id: 'u1',
			email: 'julia@example.com',
		});
	});

	it('translates 401 on login into InvalidCredentialsError', async () => {
		server.use(http.post('*/api/auth/login', () => HttpResponse.json({ detail: 'x' }, { status: 401 })));

		await expect(gateway.login({ email: 'j@example.com', password: 'wrong' })).rejects.toBeInstanceOf(
			InvalidCredentialsError,
		);
	});

	it('translates 429 on login into TooManyLoginAttemptsError with the wait time', async () => {
		server.use(
			http.post('*/api/auth/login', () =>
				HttpResponse.json({ detail: 'x' }, { status: 429, headers: { 'Retry-After': '600' } }),
			),
		);

		await expect(gateway.login({ email: 'j@example.com', password: 'wrong' })).rejects.toMatchObject({
			retryAfterSeconds: 600,
		});
		await expect(gateway.login({ email: 'j@example.com', password: 'wrong' })).rejects.toBeInstanceOf(
			TooManyLoginAttemptsError,
		);
	});

	it('returns the current user, or null on 401', async () => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
		await expect(gateway.currentUser()).resolves.toEqual({ id: 'u1', email: 'julia@example.com' });

		server.use(http.get('*/api/auth/me', () => HttpResponse.json({}, { status: 401 })));
		await expect(gateway.currentUser()).resolves.toBeNull();
	});

	it('does not hide unexpected errors from currentUser', async () => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({}, { status: 500 })));

		await expect(gateway.currentUser()).rejects.toMatchObject({ status: 500 });
	});

	it('logs out with POST /api/auth/logout', async () => {
		let called = false;
		server.use(
			http.post('*/api/auth/logout', () => {
				called = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);

		await gateway.logout();

		expect(called).toBe(true);
	});
});
