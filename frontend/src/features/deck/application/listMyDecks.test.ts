import { describe, expect, it } from 'vitest';
import { createDeck } from './createDeck';
import { FakeDeckGateway } from './FakeDeckGateway';
import { listMyDecks } from './listMyDecks';

describe('listMyDecks', () => {
	it('returns an empty list when there are no decks', async () => {
		await expect(listMyDecks(new FakeDeckGateway())).resolves.toEqual([]);
	});

	it('returns the decks of the gateway, newest first', async () => {
		const gateway = new FakeDeckGateway();
		await createDeck(gateway, { name: 'First', description: '', visibility: 'PRIVATE' });
		await createDeck(gateway, { name: 'Second', description: '', visibility: 'PUBLIC' });

		const decks = await listMyDecks(gateway);

		expect(decks.map((deck) => deck.name)).toEqual(['Second', 'First']);
	});
});
