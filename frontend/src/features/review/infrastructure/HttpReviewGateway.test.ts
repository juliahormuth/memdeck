import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import { StudyDeckNotFoundError, StudyFlashcardNotFoundError } from '../domain/errors';
import { HttpReviewGateway } from './HttpReviewGateway';

const gateway = new HttpReviewGateway();

describe('HttpReviewGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('lists the study queue with GET /api/decks/{id}/study', async () => {
		const card = { id: 'f1', front: 'Úmero?', back: 'Osso do braço', newCard: true };
		server.use(http.get('*/api/decks/d1/study', () => HttpResponse.json([card])));

		await expect(gateway.listStudyQueue('d1')).resolves.toEqual([card]);
	});

	it('translates 404 when listing the queue', async () => {
		server.use(http.get('*/api/decks/d1/study', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));

		await expect(gateway.listStudyQueue('d1')).rejects.toBeInstanceOf(StudyDeckNotFoundError);
	});

	it('records a review with the grade in the body', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/flashcards/f1/reviews', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ dueDate: '2026-01-02', intervalDays: 1 });
			}),
		);

		const result = await gateway.recordReview('d1', 'f1', 'HARD');

		expect(body).toEqual({ grade: 'HARD' });
		expect(result).toEqual({ dueDate: '2026-01-02', intervalDays: 1 });
	});

	it('translates 404 when recording a review', async () => {
		server.use(http.post('*/api/decks/d1/flashcards/f1/reviews', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));

		await expect(gateway.recordReview('d1', 'f1', 'GOOD')).rejects.toBeInstanceOf(StudyFlashcardNotFoundError);
	});

	it('lets other errors through', async () => {
		server.use(http.get('*/api/decks/d1/study', () => HttpResponse.json({ detail: 'x' }, { status: 500 })));

		await expect(gateway.listStudyQueue('d1')).rejects.not.toBeInstanceOf(StudyDeckNotFoundError);
	});

	it('gets the ranking of a deck', async () => {
		const entry = { position: 1, userId: 'u1', name: 'Julia', avatar: 'CAT', reviewedFlashcards: 3, me: true };
		server.use(http.get('*/api/decks/d1/ranking', () => HttpResponse.json([entry])));

		await expect(gateway.getRanking('d1')).resolves.toEqual([entry]);
	});
});
