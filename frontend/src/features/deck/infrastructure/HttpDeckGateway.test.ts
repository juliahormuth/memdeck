import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { AlreadyDeckMemberError, DeckNotFoundError, InvalidDeckError, NotDeckOwnerError } from '../domain/errors';
import { HttpDeckGateway } from './HttpDeckGateway';

const dto = {
	id: 'd1',
	name: 'Anatomia',
	description: 'Ossos',
	visibility: 'PRIVATE',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: true,
	role: 'OWNER',
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

	it('lists my decks with GET /api/decks and maps each one', async () => {
		server.use(
			http.get('*/api/decks', () => HttpResponse.json([dto, { ...dto, id: 'd2', name: 'English', description: null }])),
		);

		const decks = await gateway.listMine();

		expect(decks).toHaveLength(2);
		expect(decks[1]).toMatchObject({ id: 'd2', name: 'English', description: null });
	});

	it('returns an empty list when the user has no decks', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json([])));

		await expect(gateway.listMine()).resolves.toEqual([]);
	});

	it('does not hide errors when listing', async () => {
		server.use(http.get('*/api/decks', () => HttpResponse.json({}, { status: 500 })));

		await expect(gateway.listMine()).rejects.toMatchObject({ status: 500 });
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

	it('updates a deck with PUT /api/decks/{id}', async () => {
		let body: unknown;
		server.use(
			http.put('*/api/decks/d1', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...dto, name: 'Novo nome' });
			}),
		);

		const deck = await gateway.update('d1', { name: 'Novo nome', visibility: 'PRIVATE' });

		expect(body).toEqual({ name: 'Novo nome', visibility: 'PRIVATE' });
		expect(deck.name).toBe('Novo nome');
	});

	it.each([
		[400, InvalidDeckError],
		[403, NotDeckOwnerError],
		[404, DeckNotFoundError],
	])('translates a %i answer when updating', async (status, errorType) => {
		server.use(http.put('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.update('d1', { name: 'a', visibility: 'PRIVATE' })).rejects.toBeInstanceOf(errorType);
	});

	it('deletes a deck with DELETE /api/decks/{id}', async () => {
		server.use(http.delete('*/api/decks/d1', () => new HttpResponse(null, { status: 204 })));

		await expect(gateway.delete('d1')).resolves.toBeUndefined();
	});

	it.each([
		[403, NotDeckOwnerError],
		[404, DeckNotFoundError],
	])('translates a %i answer when deleting', async (status, errorType) => {
		server.use(http.delete('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.delete('d1')).rejects.toBeInstanceOf(errorType);
	});

	it('searches the public decks with the encoded term and keeps the role', async () => {
		let search: string | null = null;
		server.use(
			http.get('*/api/decks/public', ({ request }) => {
				search = new URL(request.url).searchParams.get('search');
				return HttpResponse.json([{ ...dto, ownedByMe: false, role: 'READER' }]);
			}),
		);

		const decks = await gateway.searchPublic('inglês & verbos');

		expect(search).toBe('inglês & verbos');
		expect(decks[0]).toMatchObject({ ownedByMe: false, role: 'READER' });
	});

	it('maps a missing role to null', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...dto, role: undefined })));

		await expect(gateway.findById('d1')).resolves.toMatchObject({ role: null });
	});

	it('joins a deck with POST /api/decks/{id}/members', async () => {
		server.use(http.post('*/api/decks/d1/members', () => HttpResponse.json({ ...dto, ownedByMe: false, role: 'READER' }, { status: 201 })));

		await expect(gateway.join('d1')).resolves.toMatchObject({ role: 'READER' });
	});

	it.each([
		[404, DeckNotFoundError],
		[409, AlreadyDeckMemberError],
	])('translates a %i answer when joining', async (status, errorType) => {
		server.use(http.post('*/api/decks/d1/members', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.join('d1')).rejects.toBeInstanceOf(errorType);
	});
});
