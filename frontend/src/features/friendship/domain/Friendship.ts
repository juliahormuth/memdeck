export interface Friend {
	userId: string;
	name: string;
	avatar: string;
	since: string;
}

export interface FriendActivity {
	userId: string;
	name: string;
	avatar: string;
	day: string;
	reviewedFlashcards: number;
}

export type UserRelation = 'SELF' | 'FRIEND' | 'REQUEST_SENT' | 'REQUEST_RECEIVED' | 'NONE';

export interface FoundUser {
	userId: string;
	name: string;
	username: string;
	avatar: string;
	relation: UserRelation;
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
