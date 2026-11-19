import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { server } from '../../../test/server';
import {
	InvalidInvitationError,
	InvitationAlreadyAnsweredError,
	InvitationDeckNotFoundError,
	InvitationNotFoundError,
	NotDeckOwnerError,
} from '../domain/errors';
import { HttpInvitationGateway } from './HttpInvitationGateway';

const gateway = new HttpInvitationGateway();

describe('HttpInvitationGateway', () => {
	beforeEach(() => {
		document.cookie = 'XSRF-TOKEN=token; path=/';
	});

	it('invites with POST /api/decks/{id}/invitations', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/invitations', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 202 });
			}),
		);

		await gateway.invite('d1', 'rita@example.com', 'EDITOR');

		expect(body).toEqual({ email: 'rita@example.com', role: 'EDITOR' });
	});

	it.each([
		[400, InvalidInvitationError],
		[403, NotDeckOwnerError],
		[404, InvitationDeckNotFoundError],
	])('translates a %i answer when inviting', async (status, errorType) => {
		server.use(http.post('*/api/decks/d1/invitations', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.invite('d1', 'a@b.co', 'READER')).rejects.toBeInstanceOf(errorType);
	});

	it('lists the pending invitations of a deck and cancels one', async () => {
		const invitation = { id: 'i1', inviteeName: 'Rita', role: 'READER', createdAt: '2026-01-01T12:00:00Z' };
		let cancelled = false;
		server.use(
			http.get('*/api/decks/d1/invitations', () => HttpResponse.json([invitation])),
			http.delete('*/api/decks/d1/invitations/i1', () => {
				cancelled = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);

		await expect(gateway.listForDeck('d1')).resolves.toEqual([invitation]);
		await gateway.cancel('d1', 'i1');

		expect(cancelled).toBe(true);
	});

	it('lists my invitations', async () => {
		const mine = { id: 'i1', deckId: 'd1', deckName: 'Anatomia', inviterName: 'Olivia', role: 'EDITOR', createdAt: '2026-01-01T12:00:00Z' };
		server.use(http.get('*/api/invitations', () => HttpResponse.json([mine])));

		await expect(gateway.listMine()).resolves.toEqual([mine]);
	});

	it('accepts an invitation and returns the deck and role', async () => {
		server.use(http.post('*/api/invitations/i1/accept', () => HttpResponse.json({ deckId: 'd1', role: 'EDITOR' })));

		await expect(gateway.accept('i1')).resolves.toEqual({ deckId: 'd1', role: 'EDITOR' });
	});

	it('declines an invitation', async () => {
		server.use(http.post('*/api/invitations/i1/decline', () => new HttpResponse(null, { status: 204 })));

		await expect(gateway.decline('i1')).resolves.toBeUndefined();
	});

	it.each([
		[404, InvitationNotFoundError],
		[409, InvitationAlreadyAnsweredError],
	])('translates a %i answer when answering', async (status, errorType) => {
		server.use(http.post('*/api/invitations/i1/accept', () => HttpResponse.json({ detail: 'x' }, { status })));

		await expect(gateway.accept('i1')).rejects.toBeInstanceOf(errorType);
	});
});
