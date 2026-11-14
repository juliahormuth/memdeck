import { describe, expect, it } from 'vitest';
import { DeckNotFoundError, InvalidDeckError } from '../domain/errors';
import { createDeck } from './createDeck';
import { FakeDeckGateway } from './FakeDeckGateway';
import { getDeck } from './getDeck';

describe('createDeck', () => {
	it('sends the trimmed data to the gateway and returns the saved deck', async () => {
		const gateway = new FakeDeckGateway();

		const deck = await createDeck(gateway, { name: '  Anatomia ', description: ' Ossos ', visibility: 'PRIVATE' });

		expect(gateway.created).toEqual([{ name: 'Anatomia', description: 'Ossos', visibility: 'PRIVATE' }]);
		expect(deck).toMatchObject({ name: 'Anatomia', description: 'Ossos', visibility: 'PRIVATE' });
	});

	it('omits a blank description', async () => {
		const gateway = new FakeDeckGateway();

		await createDeck(gateway, { name: 'Inglês', description: '   ', visibility: 'PUBLIC' });

		expect(gateway.created[0]).toEqual({ name: 'Inglês', description: undefined, visibility: 'PUBLIC' });
	});

	it('does not touch the gateway when the data is invalid', async () => {
		const gateway = new FakeDeckGateway();

		await expect(createDeck(gateway, { name: ' ', description: '', visibility: 'PRIVATE' })).rejects.toMatchObject({
			fields: { name: 'blank' },
		});

		expect(gateway.created).toHaveLength(0);
	});

	it('throws a domain error that carries the invalid fields', async () => {
		await expect(
			createDeck(new FakeDeckGateway(), { name: '', description: '', visibility: 'PRIVATE' }),
		).rejects.toBeInstanceOf(InvalidDeckError);
	});
});

describe('getDeck', () => {
	it('returns a deck by id', async () => {
		const gateway = new FakeDeckGateway();
		const saved = await createDeck(gateway, { name: 'Anatomia', description: '', visibility: 'PRIVATE' });

		await expect(getDeck(gateway, saved.id)).resolves.toEqual(saved);
	});

	it('fails when the deck does not exist', async () => {
		await expect(getDeck(new FakeDeckGateway(), 'missing')).rejects.toBeInstanceOf(DeckNotFoundError);
	});
});
