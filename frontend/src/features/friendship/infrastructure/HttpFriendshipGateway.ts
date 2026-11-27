import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { FriendshipGateway } from '../application/FriendshipGateway';
import { FriendshipNotFoundError, InvalidFriendRequestError, TooManySearchesError } from '../domain/errors';
import type { FoundUser, Friend, FriendActivity, FriendRequest } from '../domain/Friendship';

const base = '/api/users/me';
const requestPath = (id: string) => `${base}/friend-requests/${encodeURIComponent(id)}`;

function translate(error: unknown): unknown {
	if (error instanceof ApiError) {
		if (error.status === 400) return new InvalidFriendRequestError();
		if (error.status === 404) return new FriendshipNotFoundError();
	}
	return error;
}

export class HttpFriendshipGateway implements FriendshipGateway {
	async listFriends(): Promise<Friend[]> {
		return httpClient.get<Friend[]>(`${base}/friends`);
	}

	async listActivity(): Promise<FriendActivity[]> {
		return httpClient.get<FriendActivity[]>(`${base}/friends/activity`);
	}

	async removeFriend(userId: string): Promise<void> {
		try {
			await httpClient.delete(`${base}/friends/${encodeURIComponent(userId)}`);
		} catch (error) {
			throw translate(error);
		}
	}

	async listRequests(): Promise<FriendRequest[]> {
		return httpClient.get<FriendRequest[]>(`${base}/friend-requests`);
	}

	async searchUser(username: string): Promise<FoundUser | null> {
		try {
			const found = await httpClient.get<FoundUser[]>(`/api/users?username=${encodeURIComponent(username)}`);
			return found[0] ?? null;
		} catch (error) {
			if (error instanceof ApiError && error.status === 429) {
				throw new TooManySearchesError(error.retryAfterSeconds);
			}
			throw error;
		}
	}

	async sendRequest(username: string): Promise<void> {
		try {
			await httpClient.post(`${base}/friend-requests`, { username });
		} catch (error) {
			throw translate(error);
		}
	}

	async accept(requestId: string): Promise<void> {
		try {
			await httpClient.post(`${requestPath(requestId)}/accept`);
		} catch (error) {
			throw translate(error);
		}
	}

	async decline(requestId: string): Promise<void> {
		try {
			await httpClient.post(`${requestPath(requestId)}/decline`);
		} catch (error) {
			throw translate(error);
		}
	}

	async cancel(requestId: string): Promise<void> {
		try {
			await httpClient.delete(requestPath(requestId));
		} catch (error) {
			throw translate(error);
		}
	}
}
