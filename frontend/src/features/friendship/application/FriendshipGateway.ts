import type { Friend, FriendRequest } from '../domain/Friendship';

export interface FriendshipGateway {
	listFriends(): Promise<Friend[]>;
	removeFriend(userId: string): Promise<void>;
	listRequests(): Promise<FriendRequest[]>;
	sendRequest(email: string): Promise<void>;
	accept(requestId: string): Promise<void>;
	decline(requestId: string): Promise<void>;
	cancel(requestId: string): Promise<void>;
}
