import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { DeckGateway, NewDeck } from '../application/DeckGateway';
import type { Deck, Visibility } from '../domain/Deck';
import { DeckNotFoundError, InvalidDeckError } from '../domain/errors';

interface DeckDto {
	id: string;
	name: string;
	description: string | null;
	visibility: Visibility;
	createdAt: string;
}

function toDeck(dto: DeckDto): Deck {
	return {
		id: dto.id,
		name: dto.name,
		description: dto.description ?? null,
		visibility: dto.visibility,
		createdAt: dto.createdAt,
	};
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
