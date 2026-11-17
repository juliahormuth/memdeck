import { describe, expect, it } from 'vitest';
import { FakeDeckGateway } from './FakeDeckGateway';
import { joinDeck } from './joinDeck';
import { searchPublicDecks } from './searchPublicDecks';

describe('explore use cases', () => {
	it('trims the term before searching', async () => {
		const searched: string[] = [];
		const gateway = new FakeDeckGateway();
		gateway.searchPublic = async (term) => {
			searched.push(term);
			return [];
		};

		await searchPublicDecks(gateway, '  verbos ');

		expect(searched).toEqual(['verbos']);
	});

	it('joining a deck makes the user a reader of it', async () => {
		const gateway = new FakeDeckGateway();
		const deck = await gateway.create({ name: 'Inglês', visibility: 'PUBLIC' });

		const joined = await joinDeck(gateway, deck.id);

		expect(joined.role).toBe('READER');
	});
});
