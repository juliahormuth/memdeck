export type InvitableRole = 'EDITOR' | 'READER';

export interface DeckInvitation {
	id: string;
	inviteeName: string;
	role: InvitableRole;
	createdAt: string;
}

export interface MyInvitation {
	id: string;
	deckId: string;
	deckName: string;
	inviterName: string;
	role: InvitableRole;
	createdAt: string;
}

export interface AcceptedInvitation {
	deckId: string;
	role: InvitableRole;
}
