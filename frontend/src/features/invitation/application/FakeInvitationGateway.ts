import type { AcceptedInvitation, DeckInvitation, InvitableRole, MyInvitation } from '../domain/Invitation';
import type { InvitationGateway } from './InvitationGateway';

export class FakeInvitationGateway implements InvitationGateway {
	readonly invited: { deckId: string; email: string; role: InvitableRole }[] = [];
	readonly answered: { id: string; answer: 'accepted' | 'declined' }[] = [];
	readonly cancelled: string[] = [];

	constructor(private readonly mine: MyInvitation[] = []) {}

	async invite(deckId: string, email: string, role: InvitableRole): Promise<void> {
		this.invited.push({ deckId, email, role });
	}

	async listForDeck(): Promise<DeckInvitation[]> {
		return [];
	}

	async cancel(_deckId: string, invitationId: string): Promise<void> {
		this.cancelled.push(invitationId);
	}

	async listMine(): Promise<MyInvitation[]> {
		return this.mine;
	}

	async accept(invitationId: string): Promise<AcceptedInvitation> {
		this.answered.push({ id: invitationId, answer: 'accepted' });
		const invitation = this.mine.find((candidate) => candidate.id === invitationId);
		return { deckId: invitation?.deckId ?? '', role: invitation?.role ?? 'READER' };
	}

	async decline(invitationId: string): Promise<void> {
		this.answered.push({ id: invitationId, answer: 'declined' });
	}
}
