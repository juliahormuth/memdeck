import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { FlashcardGateway, NewFlashcard } from '../application/FlashcardGateway';
import type { Flashcard } from '../domain/Flashcard';
import { FlashcardDeckNotFoundError, FlashcardNotFoundError, InvalidFlashcardError, NotDeckOwnerError } from '../domain/errors';

interface FlashcardDto {
	id: string;
	deckId: string;
	front: string;
	back: string;
	createdAt: string;
}

function toFlashcard(dto: FlashcardDto): Flashcard {
	return { id: dto.id, deckId: dto.deckId, front: dto.front, back: dto.back, createdAt: dto.createdAt };
}

const flashcardsPath = (deckId: string) => `/api/decks/${encodeURIComponent(deckId)}/flashcards`;
const flashcardPath = (deckId: string, id: string) => `${flashcardsPath(deckId)}/${encodeURIComponent(id)}`;

function translateChangeError(error: unknown, badRequest: () => Error): unknown {
	if (error instanceof ApiError) {
		if (error.status === 400) return badRequest();
		if (error.status === 403) return new NotDeckOwnerError();
		if (error.status === 404) return new FlashcardNotFoundError();
	}
	return error;
}

export class HttpFlashcardGateway implements FlashcardGateway {
	async create(deckId: string, flashcard: NewFlashcard): Promise<Flashcard> {
		try {
			return toFlashcard(await httpClient.post<FlashcardDto>(flashcardsPath(deckId), flashcard));
		} catch (error) {
			if (error instanceof ApiError) {
				if (error.status === 400) throw new InvalidFlashcardError({});
				if (error.status === 403) throw new NotDeckOwnerError();
				if (error.status === 404) throw new FlashcardDeckNotFoundError();
			}
			throw error;
		}
	}

	async update(deckId: string, id: string, flashcard: NewFlashcard): Promise<Flashcard> {
		try {
			return toFlashcard(await httpClient.put<FlashcardDto>(flashcardPath(deckId, id), flashcard));
		} catch (error) {
			throw translateChangeError(error, () => new InvalidFlashcardError({}));
		}
	}

	async delete(deckId: string, id: string): Promise<void> {
		try {
			await httpClient.delete(flashcardPath(deckId, id));
		} catch (error) {
			throw translateChangeError(error, () => new FlashcardNotFoundError());
		}
	}

	async listByDeck(deckId: string): Promise<Flashcard[]> {
		try {
			return (await httpClient.get<FlashcardDto[]>(flashcardsPath(deckId))).map(toFlashcard);
		} catch (error) {
			if (error instanceof ApiError && (error.status === 404 || error.status === 400)) {
				throw new FlashcardDeckNotFoundError();
			}
			throw error;
		}
	}
}
