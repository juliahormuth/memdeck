import type { Deck } from '../domain/Deck';
import type { DeckGateway } from './DeckGateway';

export function listMyDecks(gateway: DeckGateway): Promise<Deck[]> {
	return gateway.listMine();
}
