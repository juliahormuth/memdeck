import { describe, expect, it } from 'vitest';
import type { MyInvitation } from '../domain/Invitation';
import { FakeInvitationGateway } from './FakeInvitationGateway';
import { inviteToDeck } from './inviteToDeck';
import { acceptInvitation, cancelInvitation, declineInvitation, listMyInvitations } from './invitationUseCases';

const mine: MyInvitation = {
	id: 'i1',
	deckId: 'd1',
	deckName: 'Anatomia',
	inviterName: 'Olivia',
	role: 'EDITOR',
	createdAt: '2026-01-01T12:00:00Z',
};

describe('invitation use cases', () => {
	it('trims the e-mail when inviting', async () => {
		const gateway = new FakeInvitationGateway();

		await inviteToDeck(gateway, { deckId: 'd1', email: '  rita@example.com ', role: 'READER' });

		expect(gateway.invited).toEqual([{ deckId: 'd1', email: 'rita@example.com', role: 'READER' }]);
	});

	it('lists my invitations and answers them', async () => {
		const gateway = new FakeInvitationGateway([mine]);

		await expect(listMyInvitations(gateway)).resolves.toEqual([mine]);
		await expect(acceptInvitation(gateway, 'i1')).resolves.toEqual({ deckId: 'd1', role: 'EDITOR' });
		await declineInvitation(gateway, 'i1');

		expect(gateway.answered).toEqual([
			{ id: 'i1', answer: 'accepted' },
			{ id: 'i1', answer: 'declined' },
		]);
	});

	it('cancels an invitation of a deck', async () => {
		const gateway = new FakeInvitationGateway();

		await cancelInvitation(gateway, 'd1', 'i9');

		expect(gateway.cancelled).toEqual(['i9']);
	});
});
