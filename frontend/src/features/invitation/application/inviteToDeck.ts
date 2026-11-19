import type { InvitableRole } from '../domain/Invitation';
import type { InvitationGateway } from './InvitationGateway';

export interface InviteToDeckCommand {
	deckId: string;
	email: string;
	role: InvitableRole;
}

export function inviteToDeck(gateway: InvitationGateway, command: InviteToDeckCommand): Promise<void> {
	return gateway.invite(command.deckId, command.email.trim(), command.role);
}
