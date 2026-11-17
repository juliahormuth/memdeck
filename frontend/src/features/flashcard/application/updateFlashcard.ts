import type { Flashcard } from '../domain/Flashcard';
import { InvalidFlashcardError } from '../domain/errors';
import { validateNewFlashcard } from '../domain/rules';
import type { FlashcardGateway } from './FlashcardGateway';

export interface UpdateFlashcardCommand {
	deckId: string;
	id: string;
	front: string;
	back: string;
}

export async function updateFlashcard(gateway: FlashcardGateway, command: UpdateFlashcardCommand): Promise<Flashcard> {
	const errors = validateNewFlashcard(command);
	if (Object.keys(errors).length > 0) {
		throw new InvalidFlashcardError(errors);
	}
	return gateway.update(command.deckId, command.id, { front: command.front.trim(), back: command.back.trim() });
}
