import type { Flashcard } from '../domain/Flashcard';
import { FlashcardNotFoundError } from '../domain/errors';
import type { FlashcardGateway, NewFlashcard } from './FlashcardGateway';

export class FakeFlashcardGateway implements FlashcardGateway {
	readonly created: { deckId: string; flashcard: NewFlashcard }[] = [];
	private readonly saved: Flashcard[] = [];

	async create(deckId: string, flashcard: NewFlashcard): Promise<Flashcard> {
		this.created.push({ deckId, flashcard });
		const result: Flashcard = {
			id: `flashcard-${this.created.length}`,
			deckId,
			front: flashcard.front,
			back: flashcard.back,
			createdAt: '2026-01-01T12:00:00Z',
		};
		this.saved.push(result);
		return result;
	}

	async update(deckId: string, id: string, flashcard: NewFlashcard): Promise<Flashcard> {
		const index = this.indexOf(deckId, id);
		const updated: Flashcard = { ...this.saved[index]!, front: flashcard.front, back: flashcard.back };
		this.saved[index] = updated;
		return updated;
	}

	async delete(deckId: string, id: string): Promise<void> {
		this.saved.splice(this.indexOf(deckId, id), 1);
	}

	private indexOf(deckId: string, id: string): number {
		const index = this.saved.findIndex((saved) => saved.id === id && saved.deckId === deckId);
		if (index < 0) {
			throw new FlashcardNotFoundError();
		}
		return index;
	}

	async listByDeck(deckId: string): Promise<Flashcard[]> {
		return this.saved.filter((flashcard) => flashcard.deckId === deckId).reverse();
	}
}
