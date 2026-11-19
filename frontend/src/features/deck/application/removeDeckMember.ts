import type { DeckGateway } from './DeckGateway';

export function removeDeckMember(gateway: DeckGateway, id: string, userId: string): Promise<void> {
	return gateway.removeMember(id, userId);
}
