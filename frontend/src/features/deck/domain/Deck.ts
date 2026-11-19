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

export interface DeckMember {
	userId: string;
	name: string;
	role: Exclude<DeckRole, 'OWNER'>;
	joinedAt: string;
}

export function canEditFlashcards(deck: Deck): boolean {
	return deck.role === 'OWNER' || deck.role === 'EDITOR';
}
