import type { Friend, FriendRequest } from '../domain/Friendship';
import type { FriendshipGateway } from './FriendshipGateway';

export class FakeFriendshipGateway implements FriendshipGateway {
	readonly sent: string[] = [];
	readonly calls: string[] = [];

	constructor(
		private readonly friends: Friend[] = [],
		private readonly requests: FriendRequest[] = [],
	) {}

	async listFriends(): Promise<Friend[]> {
		return this.friends;
	}

	async removeFriend(userId: string): Promise<void> {
		this.calls.push(`remove:${userId}`);
	}

	async listRequests(): Promise<FriendRequest[]> {
		return this.requests;
	}

	async sendRequest(email: string): Promise<void> {
		this.sent.push(email);
	}

	async accept(requestId: string): Promise<void> {
		this.calls.push(`accept:${requestId}`);
	}

	async decline(requestId: string): Promise<void> {
		this.calls.push(`decline:${requestId}`);
	}

	async cancel(requestId: string): Promise<void> {
		this.calls.push(`cancel:${requestId}`);
	}
}
