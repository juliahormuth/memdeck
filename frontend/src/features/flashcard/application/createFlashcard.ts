import type { Flashcard } from '../domain/Flashcard';
import { InvalidFlashcardError } from '../domain/errors';
import { validateNewFlashcard } from '../domain/rules';
import type { FlashcardGateway } from './FlashcardGateway';

export interface CreateFlashcardCommand {
	deckId: string;
	front: string;
	back: string;
}

export async function createFlashcard(gateway: FlashcardGateway, command: CreateFlashcardCommand): Promise<Flashcard> {
	const errors = validateNewFlashcard(command);
	if (Object.keys(errors).length > 0) {
		throw new InvalidFlashcardError(errors);
	}
	return gateway.create(command.deckId, { front: command.front.trim(), back: command.back.trim() });
}
