import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { DeckGateway, NewDeck } from '../application/DeckGateway';
import type { Deck, Visibility } from '../domain/Deck';
import { DeckNotFoundError, InvalidDeckError, NotDeckOwnerError } from '../domain/errors';

interface DeckDto {
	id: string;
	name: string;
	description: string | null;
	visibility: Visibility;
	createdAt: string;
	ownedByMe: boolean;
}

function toDeck(dto: DeckDto): Deck {
	return {
		id: dto.id,
		name: dto.name,
		description: dto.description ?? null,
		visibility: dto.visibility,
		createdAt: dto.createdAt,
		ownedByMe: dto.ownedByMe,
	};
}

const deckPath = (id: string) => `/api/decks/${encodeURIComponent(id)}`;

function translateChangeError(error: unknown, badRequest: () => Error): unknown {
	if (error instanceof ApiError) {
		if (error.status === 400) return badRequest();
		if (error.status === 403) return new NotDeckOwnerError();
		if (error.status === 404) return new DeckNotFoundError();
	}
	return error;
}

export class HttpDeckGateway implements DeckGateway {
	async create(deck: NewDeck): Promise<Deck> {
		try {
			return toDeck(await httpClient.post<DeckDto>('/api/decks', deck));
		} catch (error) {
			if (error instanceof ApiError && error.status === 400) {
				throw new InvalidDeckError({});
			}
			throw error;
		}
	}

	async update(id: string, deck: NewDeck): Promise<Deck> {
		try {
			return toDeck(await httpClient.put<DeckDto>(deckPath(id), deck));
		} catch (error) {
			throw translateChangeError(error, () => new InvalidDeckError({}));
		}
	}

	async delete(id: string): Promise<void> {
		try {
			await httpClient.delete(deckPath(id));
		} catch (error) {
			throw translateChangeError(error, () => new DeckNotFoundError());
		}
	}

	async listMine(): Promise<Deck[]> {
		return (await httpClient.get<DeckDto[]>('/api/decks')).map(toDeck);
	}

	async findById(id: string): Promise<Deck> {
		try {
			return toDeck(await httpClient.get<DeckDto>(`/api/decks/${encodeURIComponent(id)}`));
		} catch (error) {
			if (error instanceof ApiError && (error.status === 404 || error.status === 400)) {
				throw new DeckNotFoundError();
			}
			throw error;
		}
	}
}
