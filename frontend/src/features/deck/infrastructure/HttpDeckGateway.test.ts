import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { DeckNotFoundError, InvalidDeckError } from '../domain/errors';
import { HttpDeckGateway } from './HttpDeckGateway';

const dto = {
	id: 'd1',
	name: 'Anatomia',
	description: 'Ossos',
	visibility: 'PRIVATE',
	createdAt: '2026-01-01T12:00:00Z',
};

const gateway = new HttpDeckGateway();

describe('HttpDeckGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('creates a deck with POST /api/decks and maps the response', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json(dto, { status: 201 });
			}),
		);

		const deck = await gateway.create({ name: 'Anatomia', description: 'Ossos', visibility: 'PRIVATE' });

		expect(body).toEqual({ name: 'Anatomia', description: 'Ossos', visibility: 'PRIVATE' });
		expect(deck).toEqual(dto);
	});

	it('maps a missing description to null', async () => {
		server.use(http.post('*/api/decks', () => HttpResponse.json({ ...dto, description: null }, { status: 201 })));

		await expect(gateway.create({ name: 'Anatomia', visibility: 'PRIVATE' })).resolves.toMatchObject({ description: null });
	});

	it('translates 400 on create into InvalidDeckError', async () => {
		server.use(http.post('*/api/decks', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));

		await expect(gateway.create({ name: 'A', visibility: 'PRIVATE' })).rejects.toBeInstanceOf(InvalidDeckError);
	});

	it('does not hide unexpected errors on create', async () => {
		server.use(http.post('*/api/decks', () => HttpResponse.json({}, { status: 500 })));

		await expect(gateway.create({ name: 'A', visibility: 'PRIVATE' })).rejects.toMatchObject({ status: 500 });
	});

	it('finds a deck by id', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json(dto)));

		await expect(gateway.findById('d1')).resolves.toEqual(dto);
	});

	it.each([404, 400])('translates %i on find into DeckNotFoundError', async (status) => {
		server.use(http.get('*/api/decks/missing', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.findById('missing')).rejects.toBeInstanceOf(DeckNotFoundError);
	});

	it('does not hide unexpected errors on find', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({}, { status: 500 })));

		await expect(gateway.findById('d1')).rejects.toMatchObject({ status: 500 });
	});
});
