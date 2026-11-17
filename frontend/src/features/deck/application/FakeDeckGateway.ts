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
			ownedByMe: true,
		};
		this.decks.set(saved.id, saved);
		return saved;
	}

	async update(id: string, deck: NewDeck): Promise<Deck> {
		const current = await this.findById(id);
		const updated: Deck = { ...current, name: deck.name, description: deck.description ?? null, visibility: deck.visibility };
		this.decks.set(id, updated);
		return updated;
	}

	async delete(id: string): Promise<void> {
		await this.findById(id);
		this.decks.delete(id);
	}

	async listMine(): Promise<Deck[]> {
		return [...this.decks.values()].reverse();
	}

	async findById(id: string): Promise<Deck> {
		const deck = this.decks.get(id);
		if (!deck) {
			throw new DeckNotFoundError();
		}
		return deck;
	}
}
