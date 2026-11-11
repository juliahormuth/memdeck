import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { server } from '../../test/server';
import { ApiError } from './ApiError';
import { httpClient } from './httpClient';

describe('httpClient', () => {
	it('sends the CSRF cookie value in the X-XSRF-TOKEN header on POST', async () => {
		document.cookie = 'XSRF-TOKEN=abc-123; path=/';
		let header: string | null = null;
		server.use(
			http.post('*/api/echo', ({ request }) => {
				header = request.headers.get('X-XSRF-TOKEN');
				return HttpResponse.json({ ok: true });
			}),
		);

		await httpClient.post('/api/echo', { a: 1 });

		expect(header).toBe('abc-123');
	});

	it('asks the API for the CSRF cookie once when it is missing', async () => {
		let meCalls = 0;
		server.use(
			http.get('*/api/auth/me', () => {
				meCalls += 1;
				return HttpResponse.json({}, { status: 401 });
			}),
			http.post('*/api/echo', () => HttpResponse.json({ ok: true })),
		);

		await httpClient.post('/api/echo', {});

		expect(meCalls).toBe(1);
	});

	it('does not send a CSRF header on GET', async () => {
		document.cookie = 'XSRF-TOKEN=abc-123; path=/';
		let header: string | null = 'unset';
		server.use(
			http.get('*/api/thing', ({ request }) => {
				header = request.headers.get('X-XSRF-TOKEN');
				return HttpResponse.json({});
			}),
		);

		await httpClient.get('/api/thing');

		expect(header).toBeNull();
	});

	it('returns undefined for 204 responses', async () => {
		document.cookie = 'XSRF-TOKEN=abc-123; path=/';
		server.use(http.post('*/api/auth/logout', () => new HttpResponse(null, { status: 204 })));

		await expect(httpClient.post('/api/auth/logout')).resolves.toBeUndefined();
	});

	it('turns a problem details response into an ApiError', async () => {
		server.use(
			http.get('*/api/thing', () =>
				HttpResponse.json({ detail: 'Deck not found', status: 404 }, { status: 404 }),
			),
		);

		await expect(httpClient.get('/api/thing')).rejects.toMatchObject({ status: 404, message: 'Deck not found' });
	});

	it('reads Retry-After into retryAfterSeconds', async () => {
		server.use(
			http.get('*/api/thing', () =>
				HttpResponse.json({ detail: 'Too many' }, { status: 429, headers: { 'Retry-After': '90' } }),
			),
		);

		const error = await httpClient.get('/api/thing').catch((caught: unknown) => caught);

		expect(error).toBeInstanceOf(ApiError);
		expect((error as ApiError).retryAfterSeconds).toBe(90);
	});

	it('falls back to the status text when the error body is not JSON', async () => {
		server.use(http.get('*/api/thing', () => new HttpResponse('boom', { status: 500, statusText: 'Server Error' })));

		await expect(httpClient.get('/api/thing')).rejects.toMatchObject({ status: 500, message: 'Server Error' });
	});
});
