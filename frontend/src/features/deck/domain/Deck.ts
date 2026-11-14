export type Visibility = 'PUBLIC' | 'PRIVATE';

export interface Deck {
	id: string;
	name: string;
	description: string | null;
	visibility: Visibility;
	createdAt: string;
}
