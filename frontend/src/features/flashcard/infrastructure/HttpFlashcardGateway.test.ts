import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { FlashcardDeckNotFoundError, FlashcardNotFoundError, InvalidFlashcardError, NotDeckEditorError } from '../domain/errors';
import { HttpFlashcardGateway } from './HttpFlashcardGateway';

const dto = { id: 'f1', deckId: 'd1', front: 'Úmero?', back: 'Osso do braço', createdAt: '2026-01-01T12:00:00Z' };

const gateway = new HttpFlashcardGateway();

describe('HttpFlashcardGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('creates a flashcard with POST /api/decks/{id}/flashcards', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/flashcards', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json(dto, { status: 201 });
			}),
		);

		const flashcard = await gateway.create('d1', { front: 'Úmero?', back: 'Osso do braço' });

		expect(body).toEqual({ front: 'Úmero?', back: 'Osso do braço' });
		expect(flashcard).toEqual(dto);
	});

	it.each([
		[400, InvalidFlashcardError],
		[403, NotDeckEditorError],
		[404, FlashcardDeckNotFoundError],
	])('translates a %i answer when creating', async (status, errorType) => {
		server.use(http.post('*/api/decks/d1/flashcards', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.create('d1', { front: 'a', back: 'b' })).rejects.toBeInstanceOf(errorType);
	});

	it('lists the flashcards of a deck', async () => {
		server.use(http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([dto])));

		await expect(gateway.listByDeck('d1')).resolves.toEqual([dto]);
	});

	it('translates 404 when listing', async () => {
		server.use(http.get('*/api/decks/d1/flashcards', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));

		await expect(gateway.listByDeck('d1')).rejects.toBeInstanceOf(FlashcardDeckNotFoundError);
	});

	it('updates a flashcard with PUT', async () => {
		let body: unknown;
		server.use(
			http.put('*/api/decks/d1/flashcards/f1', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...dto, front: 'Rádio?' });
			}),
		);

		const flashcard = await gateway.update('d1', 'f1', { front: 'Rádio?', back: 'Antebraço' });

		expect(body).toEqual({ front: 'Rádio?', back: 'Antebraço' });
		expect(flashcard.front).toBe('Rádio?');
	});

	it.each([
		[400, InvalidFlashcardError],
		[403, NotDeckEditorError],
		[404, FlashcardNotFoundError],
	])('translates a %i answer when updating', async (status, errorType) => {
		server.use(http.put('*/api/decks/d1/flashcards/f1', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.update('d1', 'f1', { front: 'a', back: 'b' })).rejects.toBeInstanceOf(errorType);
	});

	it('deletes a flashcard with DELETE', async () => {
		server.use(http.delete('*/api/decks/d1/flashcards/f1', () => new HttpResponse(null, { status: 204 })));

		await expect(gateway.delete('d1', 'f1')).resolves.toBeUndefined();
	});

	it.each([
		[403, NotDeckEditorError],
		[404, FlashcardNotFoundError],
	])('translates a %i answer when deleting', async (status, errorType) => {
		server.use(http.delete('*/api/decks/d1/flashcards/f1', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.delete('d1', 'f1')).rejects.toBeInstanceOf(errorType);
	});
});
