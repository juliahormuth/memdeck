import { createFlashcard } from './application/createFlashcard';
import type { CreateFlashcardCommand } from './application/createFlashcard';
import { deleteFlashcard } from './application/deleteFlashcard';
import { listDeckFlashcards } from './application/listDeckFlashcards';
import { updateFlashcard } from './application/updateFlashcard';
import type { UpdateFlashcardCommand } from './application/updateFlashcard';
import { HttpFlashcardGateway } from './infrastructure/HttpFlashcardGateway';

const gateway = new HttpFlashcardGateway();

export const flashcardUseCases = {
	createFlashcard: (command: CreateFlashcardCommand) => createFlashcard(gateway, command),
	listDeckFlashcards: (deckId: string) => listDeckFlashcards(gateway, deckId),
	updateFlashcard: (command: UpdateFlashcardCommand) => updateFlashcard(gateway, command),
	deleteFlashcard: (deckId: string, id: string) => deleteFlashcard(gateway, deckId, id),
};
