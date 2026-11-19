export interface Friend {
	userId: string;
	name: string;
	avatar: string;
	since: string;
}

export type FriendRequestDirection = 'INCOMING' | 'OUTGOING';

export interface FriendRequest {
	id: string;
	direction: FriendRequestDirection;
	userId: string;
	name: string;
	avatar: string;
	createdAt: string;
}
