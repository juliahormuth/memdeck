import type { FlashcardGateway } from './FlashcardGateway';

export function deleteFlashcard(gateway: FlashcardGateway, deckId: string, id: string): Promise<void> {
	return gateway.delete(deckId, id);
}
