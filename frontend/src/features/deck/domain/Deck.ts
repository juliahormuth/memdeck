export type Visibility = 'PUBLIC' | 'PRIVATE';
export type DeckRole = 'OWNER' | 'EDITOR' | 'READER';

export interface Deck {
	id: string;
	name: string;
	description: string | null;
	visibility: Visibility;
	createdAt: string;
	ownedByMe: boolean;
	role: DeckRole | null;
}
