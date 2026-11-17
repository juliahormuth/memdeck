import { describe, expect, it } from 'vitest';
import { InvalidFlashcardError } from '../domain/errors';
import { createFlashcard } from './createFlashcard';
import { FakeFlashcardGateway } from './FakeFlashcardGateway';

describe('createFlashcard', () => {
	it('saves the flashcard in the deck with trimmed text', async () => {
		const gateway = new FakeFlashcardGateway();

		const flashcard = await createFlashcard(gateway, { deckId: 'd1', front: '  Úmero?  ', back: ' Osso do braço ' });

		expect(gateway.created).toEqual([{ deckId: 'd1', flashcard: { front: 'Úmero?', back: 'Osso do braço' } }]);
		expect(flashcard.deckId).toBe('d1');
	});

	it('does not call the gateway when the data is invalid', async () => {
		const gateway = new FakeFlashcardGateway();

		await expect(createFlashcard(gateway, { deckId: 'd1', front: ' ', back: 'x' })).rejects.toBeInstanceOf(
			InvalidFlashcardError,
		);

		expect(gateway.created).toHaveLength(0);
	});
});
