import type { DeckGateway } from './DeckGateway';

export function leaveDeck(gateway: DeckGateway, id: string): Promise<void> {
	return gateway.leave(id);
}
