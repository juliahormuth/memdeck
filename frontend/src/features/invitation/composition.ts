import { acceptInvitation, cancelInvitation, declineInvitation, listDeckInvitations, listMyInvitations } from './application/invitationUseCases';
import { inviteToDeck } from './application/inviteToDeck';
import type { InviteToDeckCommand } from './application/inviteToDeck';
import { HttpInvitationGateway } from './infrastructure/HttpInvitationGateway';

const gateway = new HttpInvitationGateway();

export const invitationUseCases = {
	inviteToDeck: (command: InviteToDeckCommand) => inviteToDeck(gateway, command),
	listDeckInvitations: (deckId: string) => listDeckInvitations(gateway, deckId),
	cancelInvitation: (deckId: string, invitationId: string) => cancelInvitation(gateway, deckId, invitationId),
	listMyInvitations: () => listMyInvitations(gateway),
	acceptInvitation: (invitationId: string) => acceptInvitation(gateway, invitationId),
	declineInvitation: (invitationId: string) => declineInvitation(gateway, invitationId),
};
