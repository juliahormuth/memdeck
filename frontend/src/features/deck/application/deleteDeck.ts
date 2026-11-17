import type { DeckGateway } from './DeckGateway';

export function deleteDeck(gateway: DeckGateway, id: string): Promise<void> {
	return gateway.delete(id);
}
