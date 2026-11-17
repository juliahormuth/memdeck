import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { DeckGateway, NewDeck } from '../application/DeckGateway';
import type { Deck, DeckRole, Visibility } from '../domain/Deck';
import { AlreadyDeckMemberError, DeckNotFoundError, InvalidDeckError, NotDeckOwnerError } from '../domain/errors';

interface DeckDto {
	id: string;
	name: string;
	description: string | null;
	visibility: Visibility;
	createdAt: string;
	ownedByMe: boolean;
	role?: DeckRole | null;
}

function toDeck(dto: DeckDto): Deck {
	return {
		id: dto.id,
		name: dto.name,
		description: dto.description ?? null,
		visibility: dto.visibility,
		createdAt: dto.createdAt,
		ownedByMe: dto.ownedByMe,
		role: dto.role ?? null,
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

	async searchPublic(term: string): Promise<Deck[]> {
		return (await httpClient.get<DeckDto[]>(`/api/decks/public?search=${encodeURIComponent(term)}`)).map(toDeck);
	}

	async join(id: string): Promise<Deck> {
		try {
			return toDeck(await httpClient.post<DeckDto>(`${deckPath(id)}/members`));
		} catch (error) {
			if (error instanceof ApiError) {
				if (error.status === 404) throw new DeckNotFoundError();
				if (error.status === 409) throw new AlreadyDeckMemberError();
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
