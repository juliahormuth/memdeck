import type { FoundUser, Friend, FriendActivity, FriendRequest } from '../domain/Friendship';
import type { FriendshipGateway } from './FriendshipGateway';

export class FakeFriendshipGateway implements FriendshipGateway {
	readonly sent: string[] = [];
	readonly calls: string[] = [];

	constructor(
		private readonly friends: Friend[] = [],
		private readonly requests: FriendRequest[] = [],
		private readonly activity: FriendActivity[] = [],
		private readonly found: FoundUser | null = null,
	) {}

	async listFriends(): Promise<Friend[]> {
		return this.friends;
	}

	async listActivity(): Promise<FriendActivity[]> {
		return this.activity;
	}

	async removeFriend(userId: string): Promise<void> {
		this.calls.push(`remove:${userId}`);
	}

	async listRequests(): Promise<FriendRequest[]> {
		return this.requests;
	}

	readonly searched: string[] = [];

	async searchUser(username: string): Promise<FoundUser | null> {
		this.searched.push(username);
		return this.found;
	}

	async sendRequest(username: string): Promise<void> {
		this.sent.push(username);
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
