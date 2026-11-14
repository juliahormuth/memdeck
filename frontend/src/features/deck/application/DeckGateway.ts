import type { Deck, Visibility } from '../domain/Deck';

export interface NewDeck {
	name: string;
	description?: string;
	visibility: Visibility;
}

export interface DeckGateway {
	create(deck: NewDeck): Promise<Deck>;
	findById(id: string): Promise<Deck>;
}
