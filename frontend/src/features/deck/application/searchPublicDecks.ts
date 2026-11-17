import type { Deck } from '../domain/Deck';
import type { DeckGateway } from './DeckGateway';

export function searchPublicDecks(gateway: DeckGateway, term: string): Promise<Deck[]> {
	return gateway.searchPublic(term.trim());
}
