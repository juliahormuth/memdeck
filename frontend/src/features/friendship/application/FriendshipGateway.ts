import type { FoundUser, Friend, FriendActivity, FriendRequest } from '../domain/Friendship';

export interface FriendshipGateway {
	listFriends(): Promise<Friend[]>;
	listActivity(): Promise<FriendActivity[]>;
	removeFriend(userId: string): Promise<void>;
	listRequests(): Promise<FriendRequest[]>;
	searchUser(username: string): Promise<FoundUser | null>;
	sendRequest(username: string): Promise<void>;
	accept(requestId: string): Promise<void>;
	decline(requestId: string): Promise<void>;
	cancel(requestId: string): Promise<void>;
}
