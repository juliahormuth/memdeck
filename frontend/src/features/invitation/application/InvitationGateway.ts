import type { AcceptedInvitation, DeckInvitation, InvitableRole, MyInvitation } from '../domain/Invitation';

export interface InvitationGateway {
	invite(deckId: string, username: string, role: InvitableRole): Promise<void>;
	listForDeck(deckId: string): Promise<DeckInvitation[]>;
	cancel(deckId: string, invitationId: string): Promise<void>;
	listMine(): Promise<MyInvitation[]>;
	accept(invitationId: string): Promise<AcceptedInvitation>;
	decline(invitationId: string): Promise<void>;
}
