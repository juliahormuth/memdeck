import type { Deck } from '../domain/Deck';
import type { DeckGateway } from './DeckGateway';

export function joinDeck(gateway: DeckGateway, id: string): Promise<Deck> {
	return gateway.join(id);
}
