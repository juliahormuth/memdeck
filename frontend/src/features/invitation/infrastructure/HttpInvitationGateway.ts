import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { InvitationGateway } from '../application/InvitationGateway';
import {
	InvalidInvitationError,
	InvitationAlreadyAnsweredError,
	InvitationDeckNotFoundError,
	InvitationNotFoundError,
	NotDeckOwnerError,
} from '../domain/errors';
import type { AcceptedInvitation, DeckInvitation, InvitableRole, MyInvitation } from '../domain/Invitation';

const deckInvitationsPath = (deckId: string) => `/api/decks/${encodeURIComponent(deckId)}/invitations`;
const answerPath = (invitationId: string, action: 'accept' | 'decline') =>
	`/api/invitations/${encodeURIComponent(invitationId)}/${action}`;

function translate(error: unknown, notFound: () => Error): unknown {
	if (error instanceof ApiError) {
		if (error.status === 400) return new InvalidInvitationError();
		if (error.status === 403) return new NotDeckOwnerError();
		if (error.status === 404) return notFound();
		if (error.status === 409) return new InvitationAlreadyAnsweredError();
	}
	return error;
}

export class HttpInvitationGateway implements InvitationGateway {
	async invite(deckId: string, email: string, role: InvitableRole): Promise<void> {
		try {
			await httpClient.post(deckInvitationsPath(deckId), { email, role });
		} catch (error) {
			throw translate(error, () => new InvitationDeckNotFoundError());
		}
	}

	async listForDeck(deckId: string): Promise<DeckInvitation[]> {
		try {
			return await httpClient.get<DeckInvitation[]>(deckInvitationsPath(deckId));
		} catch (error) {
			throw translate(error, () => new InvitationDeckNotFoundError());
		}
	}

	async cancel(deckId: string, invitationId: string): Promise<void> {
		try {
			await httpClient.delete(`${deckInvitationsPath(deckId)}/${encodeURIComponent(invitationId)}`);
		} catch (error) {
			throw translate(error, () => new InvitationNotFoundError());
		}
	}

	async listMine(): Promise<MyInvitation[]> {
		return httpClient.get<MyInvitation[]>('/api/invitations');
	}

	async accept(invitationId: string): Promise<AcceptedInvitation> {
		try {
			return await httpClient.post<AcceptedInvitation>(answerPath(invitationId, 'accept'));
		} catch (error) {
			throw translate(error, () => new InvitationNotFoundError());
		}
	}

	async decline(invitationId: string): Promise<void> {
		try {
			await httpClient.post(answerPath(invitationId, 'decline'));
		} catch (error) {
			throw translate(error, () => new InvitationNotFoundError());
		}
	}
}
