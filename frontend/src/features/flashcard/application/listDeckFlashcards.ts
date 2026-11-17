import type { Flashcard } from '../domain/Flashcard';
import type { FlashcardGateway } from './FlashcardGateway';

export function listDeckFlashcards(gateway: FlashcardGateway, deckId: string): Promise<Flashcard[]> {
	return gateway.listByDeck(deckId);
}
