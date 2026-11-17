import { describe, expect, it } from 'vitest';
import type { StudyCard } from '../domain/StudyCard';
import { FakeReviewGateway } from './FakeReviewGateway';
import { listStudyQueue } from './listStudyQueue';
import { recordReview } from './recordReview';

const card: StudyCard = { id: 'f1', front: 'Úmero?', back: 'Osso do braço', newCard: true };

describe('study use cases', () => {
	it('lists the study queue of a deck', async () => {
		await expect(listStudyQueue(new FakeReviewGateway([card]), 'd1')).resolves.toEqual([card]);
	});

	it('records the grade given to a flashcard', async () => {
		const gateway = new FakeReviewGateway([card]);

		const result = await recordReview(gateway, { deckId: 'd1', flashcardId: 'f1', grade: 'GOOD' });

		expect(gateway.recorded).toEqual([{ deckId: 'd1', flashcardId: 'f1', grade: 'GOOD' }]);
		expect(result.intervalDays).toBe(1);
	});
});
