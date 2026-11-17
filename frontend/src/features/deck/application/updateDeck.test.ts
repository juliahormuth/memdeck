import { describe, expect, it } from 'vitest';
import { InvalidDeckError } from '../domain/errors';
import { createDeck } from './createDeck';
import { deleteDeck } from './deleteDeck';
import { FakeDeckGateway } from './FakeDeckGateway';
import { updateDeck } from './updateDeck';

describe('updateDeck and deleteDeck', () => {
	it('changes the deck with trimmed name and without a blank description', async () => {
		const gateway = new FakeDeckGateway();
		const deck = await createDeck(gateway, { name: 'Anatomia', description: 'Ossos', visibility: 'PRIVATE' });

		const updated = await updateDeck(gateway, { id: deck.id, name: '  Anatomia humana ', description: '  ', visibility: 'PUBLIC' });

		expect(updated).toMatchObject({ id: deck.id, name: 'Anatomia humana', description: null, visibility: 'PUBLIC' });
	});

	it('does not change anything when the data is invalid', async () => {
		const gateway = new FakeDeckGateway();
		const deck = await createDeck(gateway, { name: 'Anatomia', description: '', visibility: 'PRIVATE' });

		await expect(updateDeck(gateway, { id: deck.id, name: ' ', description: '', visibility: 'PRIVATE' })).rejects.toBeInstanceOf(
			InvalidDeckError,
		);

		expect((await gateway.findById(deck.id)).name).toBe('Anatomia');
	});

	it('deletes the deck', async () => {
		const gateway = new FakeDeckGateway();
		const deck = await createDeck(gateway, { name: 'Anatomia', description: '', visibility: 'PRIVATE' });

		await deleteDeck(gateway, deck.id);

		await expect(gateway.findById(deck.id)).rejects.toThrow();
	});
});
