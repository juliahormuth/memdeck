import type { DeckMember } from '../domain/Deck';
import type { DeckGateway } from './DeckGateway';

export function listDeckMembers(gateway: DeckGateway, id: string): Promise<DeckMember[]> {
	return gateway.listMembers(id);
}
