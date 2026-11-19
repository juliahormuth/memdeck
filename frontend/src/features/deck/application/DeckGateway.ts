import type { Deck, DeckMember, Visibility } from '../domain/Deck';

export interface NewDeck {
	name: string;
	description?: string;
	visibility: Visibility;
}

export interface DeckGateway {
	create(deck: NewDeck): Promise<Deck>;
	update(id: string, deck: NewDeck): Promise<Deck>;
	delete(id: string): Promise<void>;
	findById(id: string): Promise<Deck>;
	listMine(): Promise<Deck[]>;
	searchPublic(term: string): Promise<Deck[]>;
	join(id: string): Promise<Deck>;
	leave(id: string): Promise<void>;
	listMembers(id: string): Promise<DeckMember[]>;
	removeMember(id: string, userId: string): Promise<void>;
}
