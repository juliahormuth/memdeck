import type { Flashcard } from '../domain/Flashcard';

export interface NewFlashcard {
	front: string;
	back: string;
}

export interface FlashcardGateway {
	create(deckId: string, flashcard: NewFlashcard): Promise<Flashcard>;
	update(deckId: string, id: string, flashcard: NewFlashcard): Promise<Flashcard>;
	delete(deckId: string, id: string): Promise<void>;
	listByDeck(deckId: string): Promise<Flashcard[]>;
}
