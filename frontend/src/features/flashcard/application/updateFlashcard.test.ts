import { describe, expect, it } from 'vitest';
import { InvalidFlashcardError } from '../domain/errors';
import { createFlashcard } from './createFlashcard';
import { deleteFlashcard } from './deleteFlashcard';
import { FakeFlashcardGateway } from './FakeFlashcardGateway';
import { updateFlashcard } from './updateFlashcard';

describe('updateFlashcard and deleteFlashcard', () => {
	it('changes the text with trimming', async () => {
		const gateway = new FakeFlashcardGateway();
		const card = await createFlashcard(gateway, { deckId: 'd1', front: 'Úmero?', back: 'Braço' });

		const updated = await updateFlashcard(gateway, { deckId: 'd1', id: card.id, front: ' Rádio? ', back: ' Antebraço ' });

		expect(updated).toMatchObject({ id: card.id, front: 'Rádio?', back: 'Antebraço' });
	});

	it('does not change anything when the data is invalid', async () => {
		const gateway = new FakeFlashcardGateway();
		const card = await createFlashcard(gateway, { deckId: 'd1', front: 'Úmero?', back: 'Braço' });

		await expect(updateFlashcard(gateway, { deckId: 'd1', id: card.id, front: ' ', back: 'x' })).rejects.toBeInstanceOf(
			InvalidFlashcardError,
		);

		expect((await gateway.listByDeck('d1'))[0]?.front).toBe('Úmero?');
	});

	it('deletes the flashcard', async () => {
		const gateway = new FakeFlashcardGateway();
		const card = await createFlashcard(gateway, { deckId: 'd1', front: 'Úmero?', back: 'Braço' });

		await deleteFlashcard(gateway, 'd1', card.id);

		expect(await gateway.listByDeck('d1')).toEqual([]);
	});
});
