import { DeckNotFoundError } from '../domain/errors';
import type { Deck, DeckMember } from '../domain/Deck';
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
			role: 'OWNER',
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

	async searchPublic(term: string): Promise<Deck[]> {
		const lowered = term.toLowerCase();
		return [...this.decks.values()].filter(
			(deck) =>
				deck.visibility === 'PUBLIC' &&
				!deck.ownedByMe &&
				(deck.name.toLowerCase().includes(lowered) || (deck.description ?? '').toLowerCase().includes(lowered)),
		);
	}

	async join(id: string): Promise<Deck> {
		const joined: Deck = { ...(await this.findById(id)), role: 'READER' };
		this.decks.set(id, joined);
		return joined;
	}

	readonly members: DeckMember[] = [];

	async leave(id: string): Promise<void> {
		const left: Deck = { ...(await this.findById(id)), role: null };
		this.decks.set(id, left);
	}

	async listMembers(id: string): Promise<DeckMember[]> {
		await this.findById(id);
		return this.members;
	}

	async removeMember(id: string, userId: string): Promise<void> {
		await this.findById(id);
		const index = this.members.findIndex((member) => member.userId === userId);
		if (index >= 0) this.members.splice(index, 1);
	}
}
