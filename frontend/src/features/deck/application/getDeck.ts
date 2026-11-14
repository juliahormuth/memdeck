import type { Deck } from '../domain/Deck';
import type { DeckGateway } from './DeckGateway';

export function getDeck(gateway: DeckGateway, id: string): Promise<Deck> {
	return gateway.findById(id);
}
