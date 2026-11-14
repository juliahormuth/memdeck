import { DeckNotFoundError } from '../domain/errors';
import type { Deck } from '../domain/Deck';
import type { DeckGateway, NewDeck } from './DeckGateway';

export class FakeDeckGateway implements DeckGateway {
	readonly created: NewDeck[] = [];
	private readonly decks = new Map<string, Deck>();

	async create(deck: NewDeck): Promise<Deck> {
		this.created.push(deck);
		const saved: Deck = {
			id: `deck-${this.created.length}`,
			name: deck.name,
			description: deck.description ?? null,
			visibility: deck.visibility,
			createdAt: '2026-01-01T12:00:00Z',
		};
		this.decks.set(saved.id, saved);
		return saved;
	}

	async findById(id: string): Promise<Deck> {
		const deck = this.decks.get(id);
		if (!deck) {
			throw new DeckNotFoundError();
		}
		return deck;
	}
}
