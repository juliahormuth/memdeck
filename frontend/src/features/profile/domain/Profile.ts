export const AVATARS = ['BOOK', 'LAYERS', 'STAR', 'GLOBE', 'CLOCK', 'PENCIL', 'PLAY', 'USERS'] as const;

export type AvatarKey = (typeof AVATARS)[number];

export interface Profile {
	id: string;
	name: string;
	email: string;
	avatar: AvatarKey;
	reviewedDays: number;
	streakDays: number;
	friends: number;
}
