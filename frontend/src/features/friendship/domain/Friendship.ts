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

export type FriendRequestDirection = 'INCOMING' | 'OUTGOING';

export interface FriendRequest {
	id: string;
	direction: FriendRequestDirection;
	userId: string;
	name: string;
	avatar: string;
	createdAt: string;
}
