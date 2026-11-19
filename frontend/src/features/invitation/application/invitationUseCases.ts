import type { AcceptedInvitation, DeckInvitation, MyInvitation } from '../domain/Invitation';
import type { InvitationGateway } from './InvitationGateway';

export function listDeckInvitations(gateway: InvitationGateway, deckId: string): Promise<DeckInvitation[]> {
	return gateway.listForDeck(deckId);
}

export function cancelInvitation(gateway: InvitationGateway, deckId: string, invitationId: string): Promise<void> {
	return gateway.cancel(deckId, invitationId);
}

export function listMyInvitations(gateway: InvitationGateway): Promise<MyInvitation[]> {
	return gateway.listMine();
}

export function acceptInvitation(gateway: InvitationGateway, invitationId: string): Promise<AcceptedInvitation> {
	return gateway.accept(invitationId);
}

export function declineInvitation(gateway: InvitationGateway, invitationId: string): Promise<void> {
	return gateway.decline(invitationId);
}
